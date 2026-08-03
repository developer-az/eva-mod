package com.eva.evamod.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/** Per-player house index + locate skip list. Persisted via NeoForge attachments. */
public class PlayerEvaData {
    public static final Codec<PlayerEvaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HouseIndexEntry.CODEC.listOf().optionalFieldOf("houses", List.of()).forGetter(d -> d.houses),
            BlockPos.CODEC.listOf().optionalFieldOf("located", List.of()).forGetter(d -> d.locatedSkips)
    ).apply(instance, PlayerEvaData::new));

    /** Only skip the same structure footprint — not an entire neighborhood. */
    private static final int SKIP_RADIUS_SQR = 20 * 20;
    private static final int MAX_LOCATED = 128;

    private final List<HouseIndexEntry> houses;
    private final List<BlockPos> locatedSkips;

    public PlayerEvaData() {
        this(List.of(), List.of());
    }

    public PlayerEvaData(List<HouseIndexEntry> houses, List<BlockPos> locatedSkips) {
        this.houses = new ArrayList<>(houses);
        this.locatedSkips = new ArrayList<>(locatedSkips);
    }

    public List<HouseIndexEntry> houses() {
        return houses;
    }

    public List<BlockPos> locatedSkips() {
        return locatedSkips;
    }

    /** Locate skip list only — meeting NPCs must not exhaust locate. */
    public boolean isLocateSkipped(BlockPos pos) {
        for (BlockPos skip : locatedSkips) {
            if (skip.distSqr(pos) <= SKIP_RADIUS_SQR) {
                return true;
            }
        }
        return false;
    }

    public void rememberLocated(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        locatedSkips.removeIf(existing -> existing.distSqr(immutable) <= SKIP_RADIUS_SQR);
        locatedSkips.add(immutable);
        while (locatedSkips.size() > MAX_LOCATED) {
            locatedSkips.removeFirst();
        }
    }

    public void resetLocated() {
        locatedSkips.clear();
    }

    public boolean meetNpc(HouseIndexEntry entry) {
        for (int i = 0; i < houses.size(); i++) {
            HouseIndexEntry existing = houses.get(i);
            if (existing.homePos().distSqr(entry.homePos()) <= SKIP_RADIUS_SQR
                    || existing.npcName().equalsIgnoreCase(entry.npcName())) {
                houses.set(i, entry);
                return false;
            }
        }
        houses.add(entry);
        return true;
    }

    public PlayerEvaData copy() {
        return new PlayerEvaData(houses, locatedSkips);
    }
}
