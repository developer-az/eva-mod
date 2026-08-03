package com.eva.evamod.entity;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * The biome family an NPC belongs to. Controls skin, name pool and job pool.
 */
public enum NpcVariant {
    PLAINS("Plains", new NpcJob[]{NpcJob.FARMER, NpcJob.SHEPHERD, NpcJob.HERBALIST}),
    DESERT("Desert", new NpcJob[]{NpcJob.CARTOGRAPHER, NpcJob.GLASSBLOWER, NpcJob.NOMAD}),
    TAIGA("Taiga", new NpcJob[]{NpcJob.LUMBERJACK, NpcJob.TRAPPER, NpcJob.HERBALIST}),
    SNOWY("Snowy", new NpcJob[]{NpcJob.TRAPPER, NpcJob.FISHERMAN, NpcJob.MINER}),
    SAVANNA("Savanna", new NpcJob[]{NpcJob.SHEPHERD, NpcJob.NOMAD, NpcJob.CARTOGRAPHER}),
    JUNGLE("Jungle", new NpcJob[]{NpcJob.HERBALIST, NpcJob.FARMER, NpcJob.FISHERMAN}),
    SWAMP("Swamp", new NpcJob[]{NpcJob.FISHERMAN, NpcJob.HERBALIST, NpcJob.MINER});

    private final String displayName;
    private final NpcJob[] jobs;

    NpcVariant(String displayName, NpcJob[] jobs) {
        this.displayName = displayName;
        this.jobs = jobs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NpcJob randomJob(RandomSource random) {
        return jobs[random.nextInt(jobs.length)];
    }

    public static NpcVariant byId(int id) {
        NpcVariant[] values = values();
        return values[Math.floorMod(id, values.length)];
    }

    public static NpcVariant fromBiome(Holder<Biome> biome) {
        if (biome.is(Biomes.DESERT)) {
            return DESERT;
        }
        if (biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES) || biome.is(Biomes.SNOWY_TAIGA)
                || biome.is(Biomes.SNOWY_BEACH) || biome.is(Biomes.GROVE) || biome.is(Biomes.SNOWY_SLOPES)) {
            return SNOWY;
        }
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            return JUNGLE;
        }
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            return SAVANNA;
        }
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return SWAMP;
        }
        if (biome.is(BiomeTags.IS_TAIGA)) {
            return TAIGA;
        }
        return PLAINS;
    }
}
