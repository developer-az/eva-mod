package com.eva.evamod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/** World-scoped cache of previously located npc_house / npc_town positions. */
public class SettlementCache {
    public static final Codec<SettlementCache> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().optionalFieldOf("houses", List.of()).forGetter(d -> d.houses),
            BlockPos.CODEC.listOf().optionalFieldOf("towns", List.of()).forGetter(d -> d.towns)
    ).apply(instance, SettlementCache::new));

    private static final int MAX_ENTRIES = 256;
    private static final int DEDUPE_RADIUS_SQR = 24 * 24;

    private final List<BlockPos> houses;
    private final List<BlockPos> towns;

    public SettlementCache() {
        this(List.of(), List.of());
    }

    public SettlementCache(List<BlockPos> houses, List<BlockPos> towns) {
        this.houses = new ArrayList<>(houses);
        this.towns = new ArrayList<>(towns);
    }

    public List<BlockPos> houses() {
        return houses;
    }

    public List<BlockPos> towns() {
        return towns;
    }

    public void remember(BlockPos pos, boolean town) {
        BlockPos immutable = pos.immutable();
        List<BlockPos> list = town ? towns : houses;
        list.removeIf(existing -> existing.distSqr(immutable) <= DEDUPE_RADIUS_SQR);
        // Drop from the other list if it was misclassified before.
        (town ? houses : towns).removeIf(existing -> existing.distSqr(immutable) <= DEDUPE_RADIUS_SQR);
        list.add(immutable);
        while (list.size() > MAX_ENTRIES) {
            list.removeFirst();
        }
    }

    public SettlementCache copy() {
        return new SettlementCache(houses, towns);
    }
}
