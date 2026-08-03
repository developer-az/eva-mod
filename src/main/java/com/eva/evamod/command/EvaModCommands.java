package com.eva.evamod.command;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = EvaMod.MODID)
public final class EvaModCommands {
    public static final ResourceKey<Structure> NPC_HOUSE =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));
    public static final ResourceKey<Structure> NPC_TOWN =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_town"));
    public static final TagKey<Structure> NPC_SETTLEMENT_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));

    private static final int LOCATE_CHUNK_RADIUS = 128;
    private static final int PROBE_STEP_CHUNKS = 24;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(Commands.literal("evamod")
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("locate")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> locate(ctx.getSource()))
                        .then(Commands.literal("reset")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> locateReset(ctx.getSource()))))
                .then(Commands.literal("near")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> near(ctx.getSource())))
                .then(Commands.literal("houses")
                        .executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("index")
                        .executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("tp")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> visit(ctx.getSource())))
                .then(Commands.literal("visit")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> visit(ctx.getSource())))
                .executes(ctx -> help(ctx.getSource())));
        dispatcher.register(Commands.literal("eva").redirect(root));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Eva Mod commands:").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("/evamod help — this list"), false);
        source.sendSuccess(() -> Component.literal("/evamod locate — next nearest house/town (skips found)"), false);
        source.sendSuccess(() -> Component.literal("/evamod locate reset — clear locate skip list"), false);
        source.sendSuccess(() -> Component.literal("/evamod near — nearby Biome NPCs"), false);
        source.sendSuccess(() -> Component.literal("/evamod houses — personal house index (met NPCs)"), false);
        source.sendSuccess(() -> Component.literal("/evamod visit — teleport to nearest house (op)"), false);
        return 1;
    }

    private static int locate(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        ServerLevel level = player.level();
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        LocateResult found = findNextSettlement(level, player.blockPosition(), data);
        if (found == null) {
            source.sendFailure(Component.literal(
                    "No new npc_house/npc_town found within " + LOCATE_CHUNK_RADIUS
                            + " chunks. Try traveling or /evamod locate reset."));
            return 0;
        }
        data.rememberLocated(found.pos());
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        String label = found.town() ? "npc_town (hamlet)" : "npc_house";
        int npcHint = found.town() ? countNpcsNear(level, found.pos(), 48) : countNpcsNear(level, found.pos(), 16);
        source.sendSuccess(() -> {
            MutableComponent msg = Component.literal("Found " + label + " at ").withStyle(ChatFormatting.GREEN);
            msg = clickablePos(msg, found.pos());
            if (found.town()) {
                msg.append(Component.literal(npcHint > 0
                        ? " — town with " + npcHint + " NPC(s) nearby"
                        : " — multi-NPC hamlet").withStyle(ChatFormatting.GRAY));
            }
            return msg;
        }, false);
        return 1;
    }

    private static int locateReset(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        data.resetLocated();
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        source.sendSuccess(() -> Component.literal("Locate skip list cleared. House index kept."), false);
        return 1;
    }

    private static int near(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        AABB box = player.getBoundingBox().inflate(64.0);
        List<BiomeNpc> npcs = player.level().getEntitiesOfClass(BiomeNpc.class, box, Entity::isAlive);
        npcs.sort(Comparator.comparingDouble(player::distanceToSqr));
        if (npcs.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No Biome NPCs within 64 blocks."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Nearby NPCs (" + npcs.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (BiomeNpc npc : npcs.stream().limit(12).toList()) {
            BlockPos pos = npc.blockPosition();
            String line = npc.getNpcName() + " — " + npc.getPersonality().getDisplayName()
                    + " " + npc.getJob().getDisplayName();
            source.sendSuccess(() -> clickablePos(Component.literal(line + " @ "), pos), false);
        }
        return npcs.size();
    }

    private static int houses(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        List<HouseIndexEntry> houses = player.getData(ModAttachments.PLAYER_DATA).houses();
        if (houses.isEmpty()) {
            source.sendSuccess(() -> Component.literal("House index empty — talk to NPCs to meet them."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("House Index (" + houses.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (HouseIndexEntry entry : houses) {
            String label = entry.npcName() + " — " + entry.personality() + " " + entry.biome()
                    + " " + entry.job() + " @ ";
            source.sendSuccess(() -> clickablePos(Component.literal(label), entry.homePos()), false);
        }
        return houses.size();
    }

    private static int visit(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        ServerLevel level = player.level();
        BlockPos found = level.findNearestMapStructure(NPC_SETTLEMENT_TAG, player.blockPosition(), 100, false);
        if (found == null) {
            source.sendFailure(Component.literal("No npc_house/npc_town found nearby."));
            return 0;
        }
        player.teleportTo(found.getX() + 0.5, found.getY() + 1.0, found.getZ() + 0.5);
        BlockPos finalFound = found;
        source.sendSuccess(() -> clickablePos(
                Component.literal("Visited settlement at ").withStyle(ChatFormatting.AQUA), finalFound), false);
        return 1;
    }

    private static LocateResult findNextSettlement(ServerLevel level, BlockPos origin, PlayerEvaData data) {
        // Prefer accurate centers from already-generated chunks.
        LocateResult loaded = findInLoadedChunks(level, origin, data);
        if (loaded != null) {
            return loaded;
        }

        // Grid of findNearest probes so skipped nearest hits don't exhaust the search.
        Set<Long> seen = new HashSet<>();
        LocateResult best = null;
        double bestDist = Double.MAX_VALUE;
        for (int cx = -LOCATE_CHUNK_RADIUS; cx <= LOCATE_CHUNK_RADIUS; cx += PROBE_STEP_CHUNKS) {
            for (int cz = -LOCATE_CHUNK_RADIUS; cz <= LOCATE_CHUNK_RADIUS; cz += PROBE_STEP_CHUNKS) {
                BlockPos probe = new BlockPos(origin.getX() + (cx << 4), origin.getY(), origin.getZ() + (cz << 4));
                BlockPos hit = level.findNearestMapStructure(NPC_SETTLEMENT_TAG, probe, PROBE_STEP_CHUNKS + 16, false);
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
            }
        }
        return best;
    }

    private static LocateResult findInLoadedChunks(ServerLevel level, BlockPos origin, PlayerEvaData data) {
        List<Holder.Reference<Structure>> structures = new ArrayList<>();
        try {
            structures.add(level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(NPC_HOUSE));
            structures.add(level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(NPC_TOWN));
        } catch (Exception e) {
            return null;
        }

        LocateResult best = null;
        double bestDist = Double.MAX_VALUE;
        int ringMax = 64;
        for (int ring = 0; ring <= ringMax; ring++) {
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
                            best = new LocateResult(center, structure.key().equals(NPC_TOWN));
                        }
                    }
                }
            }
            if (best != null && ring >= 8) {
                return best;
            }
        }
        return best;
    }

    private static LocateResult refineResult(ServerLevel level, BlockPos hit) {
        try {
            Holder.Reference<Structure> town =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(NPC_TOWN);
            StructureStart townStart = level.structureManager().getStructureAt(hit, town.value());
            if (townStart != null && townStart.isValid()) {
                return new LocateResult(townStart.getBoundingBox().getCenter(), true);
            }
            Holder.Reference<Structure> house =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(NPC_HOUSE);
            StructureStart houseStart = level.structureManager().getStructureAt(hit, house.value());
            if (houseStart != null && houseStart.isValid()) {
                return new LocateResult(houseStart.getBoundingBox().getCenter(), false);
            }
        } catch (Exception ignored) {
            // Unloaded / unavailable — fall through with map position.
        }
        // Heuristic: towns are larger; if several NPCs are clustered, call it a town.
        boolean town = countNpcsNear(level, hit, 40) >= 3;
        return new LocateResult(hit, town);
    }

    private static int countNpcsNear(ServerLevel level, BlockPos pos, int radius) {
        if (!level.hasChunkAt(pos)) {
            return 0;
        }
        AABB box = new AABB(pos).inflate(radius);
        return level.getEntitiesOfClass(BiomeNpc.class, box, Entity::isAlive).size();
    }

    private static MutableComponent clickablePos(Component prefix, BlockPos pos) {
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return Component.empty().append(prefix).append(Component.literal(coords).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + coords))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to suggest teleport")))));
    }

    private record LocateResult(BlockPos pos, boolean town) {
    }

    private EvaModCommands() {
    }
}
