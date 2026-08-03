package com.eva.evamod.entity;

import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What an NPC does all day. Controls held item, the job block placed in their
 * house, which blocks they wander to for "work" and their trade pool.
 */
public enum NpcJob {
    FARMER("Farmer", Items.IRON_HOE, Blocks.COMPOSTER,
            state -> state.getBlock() instanceof CropBlock || state.is(Blocks.FARMLAND)),
    SHEPHERD("Shepherd", Items.SHEARS, Blocks.LOOM,
            state -> state.is(Blocks.GRASS_BLOCK)),
    FISHERMAN("Fisherman", Items.FISHING_ROD, Blocks.BARREL,
            state -> state.is(Blocks.WATER)),
    LUMBERJACK("Lumberjack", Items.IRON_AXE, Blocks.CRAFTING_TABLE,
            state -> state.is(BlockTags.LOGS)),
    TRAPPER("Trapper", Items.LEAD, Blocks.SMOKER,
            state -> state.is(BlockTags.LEAVES)),
    HERBALIST("Herbalist", Items.POPPY, Blocks.BREWING_STAND,
            state -> state.is(BlockTags.FLOWERS)),
    MINER("Miner", Items.IRON_PICKAXE, Blocks.STONECUTTER,
            state -> state.is(BlockTags.BASE_STONE_OVERWORLD)),
    CARTOGRAPHER("Cartographer", Items.PAPER, Blocks.CARTOGRAPHY_TABLE,
            state -> false),
    GLASSBLOWER("Glassblower", Items.GLASS_BOTTLE, Blocks.FURNACE,
            state -> state.is(BlockTags.SAND)),
    NOMAD("Nomad", Items.STICK, Blocks.CAULDRON,
            state -> false);

    private final String displayName;
    private final Item workItem;
    private final Block jobBlock;
    private final Predicate<BlockState> workTarget;

    NpcJob(String displayName, Item workItem, Block jobBlock, Predicate<BlockState> workTarget) {
        this.displayName = displayName;
        this.workItem = workItem;
        this.jobBlock = jobBlock;
        this.workTarget = workTarget;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Item getWorkItem() {
        return workItem;
    }

    public Block getJobBlock() {
        return jobBlock;
    }

    public boolean isWorkTarget(BlockState state) {
        return workTarget.test(state);
    }

    public static NpcJob byId(int id) {
        NpcJob[] values = values();
        return values[Math.floorMod(id, values.length)];
    }
}
