package com.eva.evamod.command;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.world.SettlementCache;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Chunked settlement search — avoids stalling the main thread with a full 128-chunk grid.
 */
@EventBusSubscriber(modid = EvaMod.MODID)
public final class SettlementLocator {
    static final int LOCATE_CHUNK_RADIUS = 128;
    private static final int PROBE_STEP_CHUNKS = 32;
    private static final int PROBES_PER_TICK = 6;
    private static final int LOADED_RING_SOFT = 12;
    private static final int LOADED_RING_HARD = 32;

    private static final Map<UUID, SearchJob> JOBS = new ConcurrentHashMap<>();

    private SettlementLocator() {
    }

    public static boolean isSearching(UUID playerId) {
        return JOBS.containsKey(playerId);
    }

    public static void cancel(UUID playerId) {
        JOBS.remove(playerId);
    }

    /** Prefer cache / loaded chunks; otherwise start a progressive probe job. */
    public static LocateResult tryInstant(ServerLevel level, BlockPos origin, PlayerEvaData data) {
        LocateResult cached = findInCache(level, origin, data);
        if (cached != null) {
            return cached;
        }
        return findInLoadedChunks(level, origin, data);
    }

    public static void startSearch(ServerPlayer player, PlayerEvaData data) {
        JOBS.put(player.getUUID(), new SearchJob(player.getUUID(), player.blockPosition().immutable(), data.copy()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (JOBS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, SearchJob>> it = JOBS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, SearchJob> entry = it.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }
            SearchJob job = entry.getValue();
            LocateResult found = job.tick(player.level());
            if (found != null) {
                it.remove();
                finishSuccess(player, found);
            } else if (job.done()) {
                it.remove();
                player.sendSystemMessage(Component.literal(
                        "No new npc_house/npc_town found within " + LOCATE_CHUNK_RADIUS
                                + " chunks. Travel farther, explore new biomes, then try /evamod locate again"
                                + " — or /evamod locate reset if you already found nearby ones.")
                        .withStyle(ChatFormatting.RED));
            } else if (job.shouldAnnounceProgress()) {
                player.sendSystemMessage(Component.literal(
                        "Still searching for houses… " + job.progressPercent()
                                + "% (safe to keep playing; you do not need cheats for this)")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static void finishSuccess(ServerPlayer player, LocateResult found) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        data.rememberLocated(found.pos());
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        rememberWorldCache(player.level(), found);

        String label = found.town() ? "npc_town (hamlet)" : "npc_house";
        int npcHint = found.town()
                ? countNpcsNear(player.level(), found.pos(), 48)
                : countNpcsNear(player.level(), found.pos(), 16);
        MutableComponent msg = Component.literal("Found " + label + " at ").withStyle(ChatFormatting.GREEN);
        msg = clickablePos(msg, found.pos());
        if (found.town()) {
            msg.append(Component.literal(npcHint > 0
                    ? " — town with " + npcHint + " NPC(s) nearby"
                    : " — multi-NPC hamlet").withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(msg);
    }

    static void rememberWorldCache(ServerLevel level, LocateResult found) {
        SettlementCache cache = level.getData(ModAttachments.SETTLEMENT_CACHE);
        cache.remember(found.pos(), found.town());
        level.setData(ModAttachments.SETTLEMENT_CACHE, cache.copy());
    }

    private static LocateResult findInCache(ServerLevel level, BlockPos origin, PlayerEvaData data) {
        SettlementCache cache = level.getData(ModAttachments.SETTLEMENT_CACHE);
        LocateResult best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : cache.towns()) {
            if (data.isLocateSkipped(pos)) {
                continue;
            }
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = new LocateResult(pos, true);
            }
        }
        for (BlockPos pos : cache.houses()) {
            if (data.isLocateSkipped(pos)) {
                continue;
            }
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = new LocateResult(pos, false);
            }
        }
        return best;
    }

    static LocateResult findInLoadedChunks(ServerLevel level, BlockPos origin, PlayerEvaData data) {
        List<Holder.Reference<Structure>> structures = new ArrayList<>();
        try {
            structures.add(level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(EvaModCommands.NPC_HOUSE));
            structures.add(level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(EvaModCommands.NPC_TOWN));
        } catch (Exception e) {
            return null;
        }

        LocateResult best = null;
        double bestDist = Double.MAX_VALUE;
        for (int ring = 0; ring <= LOADED_RING_HARD; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (ring > 0 && Math.abs(dx) != ring && Math.abs(dz) != ring) {
                        continue;
                    }
                    int chunkX = (origin.getX() >> 4) + dx;
                    int chunkZ = (origin.getZ() >> 4) + dz;
                    if (!level.hasChunk(chunkX, chunkZ)) {
                        continue;
                    }
                    BlockPos sample = new BlockPos((chunkX << 4) + 8, origin.getY(), (chunkZ << 4) + 8);
                    for (Holder.Reference<Structure> structure : structures) {
                        StructureStart start = level.structureManager().getStructureAt(sample, structure.value());
                        if (start == null || !start.isValid()) {
                            continue;
                        }
                        BlockPos center = start.getBoundingBox().getCenter();
                        if (data.isLocateSkipped(center)) {
                            continue;
                        }
                        double dist = center.distSqr(origin);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = new LocateResult(center, structure.key().equals(EvaModCommands.NPC_TOWN));
                        }
                    }
                }
            }
            if (best != null && ring >= LOADED_RING_SOFT) {
                return best;
            }
        }
        return best;
    }

    static LocateResult refineResult(ServerLevel level, BlockPos hit) {
        try {
            Holder.Reference<Structure> town =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(EvaModCommands.NPC_TOWN);
            StructureStart townStart = level.structureManager().getStructureAt(hit, town.value());
            if (townStart != null && townStart.isValid()) {
                return new LocateResult(townStart.getBoundingBox().getCenter(), true);
            }
            Holder.Reference<Structure> house =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(EvaModCommands.NPC_HOUSE);
            StructureStart houseStart = level.structureManager().getStructureAt(hit, house.value());
            if (houseStart != null && houseStart.isValid()) {
                return new LocateResult(houseStart.getBoundingBox().getCenter(), false);
            }
        } catch (Exception ignored) {
            // Unloaded / unavailable — fall through with map position.
        }
        boolean town = countNpcsNear(level, hit, 40) >= 3;
        return new LocateResult(hit, town);
    }

    static int countNpcsNear(ServerLevel level, BlockPos pos, int radius) {
        if (!level.hasChunkAt(pos)) {
            return 0;
        }
        AABB box = new AABB(pos).inflate(radius);
        return level.getEntitiesOfClass(BiomeNpc.class, box, Entity::isAlive).size();
    }

    /** Safe landing near a structure map position (surface height, 2-block clearance). */
    static BlockPos safeTeleportPos(ServerLevel level, BlockPos rough) {
        int x = rough.getX();
        int z = rough.getZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y < level.getMinY() + 1) {
            y = Math.max(level.getMinY() + 1, rough.getY());
        }
        BlockPos feet = new BlockPos(x, y, z);
        if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) {
            for (int dy = 1; dy <= 8; dy++) {
                BlockPos up = feet.above(dy);
                if (level.getBlockState(up).isAir() && level.getBlockState(up.above()).isAir()) {
                    return up;
                }
            }
        }
        return feet;
    }

    static MutableComponent clickablePos(Component prefix, BlockPos pos) {
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return Component.empty().append(prefix).append(Component.literal(coords).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + coords))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to suggest teleport")))));
    }

    record LocateResult(BlockPos pos, boolean town) {
    }

    private static final class SearchJob {
        private final UUID playerId;
        private final BlockPos origin;
        private final PlayerEvaData data;
        private final List<BlockPos> probes = new ArrayList<>();
        private final Set<Long> seen = new HashSet<>();
        private int index;
        private int ticks;
        private LocateResult best;
        private double bestDist = Double.MAX_VALUE;
        private int lastAnnouncePercent = -10;

        private SearchJob(UUID playerId, BlockPos origin, PlayerEvaData data) {
            this.playerId = playerId;
            this.origin = origin;
            this.data = data;
            // Spiral / ring order so nearer probes run first; stop early when we have a hit.
            for (int ring = 0; ring <= LOCATE_CHUNK_RADIUS; ring += PROBE_STEP_CHUNKS) {
                if (ring == 0) {
                    probes.add(origin);
                    continue;
                }
                for (int cx = -ring; cx <= ring; cx += PROBE_STEP_CHUNKS) {
                    probes.add(new BlockPos(origin.getX() + (cx << 4), origin.getY(), origin.getZ() + (-ring << 4)));
                    probes.add(new BlockPos(origin.getX() + (cx << 4), origin.getY(), origin.getZ() + (ring << 4)));
                }
                for (int cz = -ring + PROBE_STEP_CHUNKS; cz <= ring - PROBE_STEP_CHUNKS; cz += PROBE_STEP_CHUNKS) {
                    probes.add(new BlockPos(origin.getX() + (-ring << 4), origin.getY(), origin.getZ() + (cz << 4)));
                    probes.add(new BlockPos(origin.getX() + (ring << 4), origin.getY(), origin.getZ() + (cz << 4)));
                }
            }
            probes.sort(Comparator.comparingDouble(p -> p.distSqr(origin)));
        }

        private LocateResult tick(ServerLevel level) {
            ticks++;
            int budget = PROBES_PER_TICK;
            while (budget-- > 0 && index < probes.size()) {
                BlockPos probe = probes.get(index++);
                BlockPos hit = level.findNearestMapStructure(
                        EvaModCommands.NPC_SETTLEMENT_TAG, probe, PROBE_STEP_CHUNKS + 16, false);
                if (hit == null || data.isLocateSkipped(hit)) {
                    continue;
                }
                long key = BlockPos.asLong(hit.getX(), 0, hit.getZ());
                if (!seen.add(key)) {
                    continue;
                }
                LocateResult refined = refineResult(level, hit);
                double dist = refined.pos().distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = refined;
                }
                // Near-enough hit on an inner ring: return without finishing the whole grid.
                int probeChunkDist = Math.max(
                        Math.abs((probe.getX() - origin.getX()) >> 4),
                        Math.abs((probe.getZ() - origin.getZ()) >> 4));
                if (best != null && probeChunkDist >= 32) {
                    return best;
                }
            }
            if (done() && best != null) {
                return best;
            }
            // Early exit once we have a candidate and remaining probes are farther than it.
            if (best != null && index < probes.size()) {
                BlockPos next = probes.get(index);
                if (next.distSqr(origin) >= bestDist) {
                    return best;
                }
            }
            return null;
        }

        private boolean done() {
            return index >= probes.size();
        }

        private int progressPercent() {
            if (probes.isEmpty()) {
                return 100;
            }
            return Math.min(99, (index * 100) / probes.size());
        }

        private boolean shouldAnnounceProgress() {
            int pct = progressPercent();
            if (pct >= lastAnnouncePercent + 25) {
                lastAnnouncePercent = pct;
                return true;
            }
            return false;
        }
    }
}
