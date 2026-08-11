package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcVariant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModLootTables {
    public static final ResourceKey<LootTable> HOUSE_PLAINS = key("chests/house_plains");
    public static final ResourceKey<LootTable> HOUSE_DESERT = key("chests/house_desert");
    public static final ResourceKey<LootTable> HOUSE_TAIGA = key("chests/house_taiga");
    public static final ResourceKey<LootTable> HOUSE_SNOWY = key("chests/house_snowy");
    public static final ResourceKey<LootTable> HOUSE_SAVANNA = key("chests/house_savanna");
    public static final ResourceKey<LootTable> HOUSE_JUNGLE = key("chests/house_jungle");
    public static final ResourceKey<LootTable> HOUSE_SWAMP = key("chests/house_swamp");
    public static final ResourceKey<LootTable> HOUSE_OCEAN = key("chests/house_ocean");
    public static final ResourceKey<LootTable> HOUSE_CHERRY = key("chests/house_cherry");
    public static final ResourceKey<LootTable> HOUSE_BADLANDS = key("chests/house_badlands");
    public static final ResourceKey<LootTable> HOUSE_MUSHROOM = key("chests/house_mushroom");
    public static final ResourceKey<LootTable> HOUSE_DARK = key("chests/house_dark");
    public static final ResourceKey<LootTable> HOUSE_MOUNTAIN = key("chests/house_mountain");

    public static final ResourceKey<LootTable> JOB_FARMER = key("chests/job_farmer");
    public static final ResourceKey<LootTable> JOB_SHEPHERD = key("chests/job_shepherd");
    public static final ResourceKey<LootTable> JOB_FISHERMAN = key("chests/job_fisherman");
    public static final ResourceKey<LootTable> JOB_LUMBERJACK = key("chests/job_lumberjack");
    public static final ResourceKey<LootTable> JOB_TRAPPER = key("chests/job_trapper");
    public static final ResourceKey<LootTable> JOB_HERBALIST = key("chests/job_herbalist");
    public static final ResourceKey<LootTable> JOB_MINER = key("chests/job_miner");
    public static final ResourceKey<LootTable> JOB_CARTOGRAPHER = key("chests/job_cartographer");
    public static final ResourceKey<LootTable> JOB_GLASSBLOWER = key("chests/job_glassblower");
    public static final ResourceKey<LootTable> JOB_NOMAD = key("chests/job_nomad");
    public static final ResourceKey<LootTable> JOB_BAKER = key("chests/job_baker");
    public static final ResourceKey<LootTable> JOB_BEEKEEPER = key("chests/job_beekeeper");
    public static final ResourceKey<LootTable> JOB_STORYTELLER = key("chests/job_storyteller");
    public static final ResourceKey<LootTable> JOB_ARCHAEOLOGIST = key("chests/job_archaeologist");

    public static ResourceKey<LootTable> forVariant(NpcVariant variant) {
        return switch (variant) {
            case DESERT -> HOUSE_DESERT;
            case TAIGA -> HOUSE_TAIGA;
            case SNOWY -> HOUSE_SNOWY;
            case SAVANNA -> HOUSE_SAVANNA;
            case JUNGLE -> HOUSE_JUNGLE;
            case SWAMP -> HOUSE_SWAMP;
            case OCEAN -> HOUSE_OCEAN;
            case CHERRY -> HOUSE_CHERRY;
            case BADLANDS -> HOUSE_BADLANDS;
            case MUSHROOM -> HOUSE_MUSHROOM;
            case DARK -> HOUSE_DARK;
            case MOUNTAIN -> HOUSE_MOUNTAIN;
            default -> HOUSE_PLAINS;
        };
    }

    public static ResourceKey<LootTable> forJob(NpcJob job) {
        return switch (job) {
            case FARMER -> JOB_FARMER;
            case SHEPHERD -> JOB_SHEPHERD;
            case FISHERMAN -> JOB_FISHERMAN;
            case LUMBERJACK -> JOB_LUMBERJACK;
            case TRAPPER -> JOB_TRAPPER;
            case HERBALIST -> JOB_HERBALIST;
            case MINER -> JOB_MINER;
            case CARTOGRAPHER -> JOB_CARTOGRAPHER;
            case GLASSBLOWER -> JOB_GLASSBLOWER;
            case NOMAD -> JOB_NOMAD;
            case BAKER -> JOB_BAKER;
            case BEEKEEPER -> JOB_BEEKEEPER;
            case STORYTELLER -> JOB_STORYTELLER;
            case ARCHAEOLOGIST -> JOB_ARCHAEOLOGIST;
        };
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(EvaMod.MODID, path));
    }

    private ModLootTables() {
    }
}
