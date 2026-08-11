package com.eva.evamod.worldgen;

import com.eva.evamod.entity.NpcVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record HousePalette(
        BlockState foundation,
        BlockState floor,
        BlockState post,
        BlockState wall,
        BlockState roof,
        BlockState roofCap,
        BlockState window,
        Block door,
        Block bed) {

    public static HousePalette of(NpcVariant variant, int alt) {
        boolean b = alt != 0;
        return switch (variant) {
            case PLAINS -> b
                    ? new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.BIRCH_PLANKS), s(Blocks.STRIPPED_BIRCH_LOG),
                    s(Blocks.BIRCH_PLANKS), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_SLAB),
                    s(Blocks.GLASS), Blocks.BIRCH_DOOR, Blocks.BED.pick(DyeColor.YELLOW))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.OAK_PLANKS), s(Blocks.OAK_LOG),
                    s(Blocks.OAK_PLANKS), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_SLAB),
                    s(Blocks.GLASS), Blocks.OAK_DOOR, Blocks.BED.pick(DyeColor.RED));
            case DESERT -> b
                    ? new HousePalette(s(Blocks.MUD_BRICKS), s(Blocks.SMOOTH_SANDSTONE), s(Blocks.CUT_SANDSTONE),
                    s(Blocks.TERRACOTTA), s(Blocks.MUD_BRICKS), s(Blocks.MUD_BRICK_SLAB),
                    s(Blocks.GLASS), Blocks.ACACIA_DOOR, Blocks.BED.pick(DyeColor.ORANGE))
                    : new HousePalette(s(Blocks.SANDSTONE), s(Blocks.SMOOTH_SANDSTONE), s(Blocks.CUT_SANDSTONE),
                    s(Blocks.SANDSTONE), s(Blocks.SMOOTH_SANDSTONE), s(Blocks.SMOOTH_SANDSTONE_SLAB),
                    s(Blocks.GLASS), Blocks.OAK_DOOR, Blocks.BED.pick(DyeColor.CYAN));
            case TAIGA -> b
                    ? new HousePalette(s(Blocks.MOSSY_COBBLESTONE), s(Blocks.SPRUCE_PLANKS), s(Blocks.STRIPPED_SPRUCE_LOG),
                    s(Blocks.STRIPPED_SPRUCE_LOG), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_SLAB),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.GREEN))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_LOG),
                    s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_SLAB),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.BLUE));
            case SNOWY -> b
                    ? new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_LOG),
                    s(Blocks.SNOW_BLOCK), s(Blocks.SNOW_BLOCK), s(Blocks.SNOW_BLOCK),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.LIGHT_BLUE))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_LOG),
                    s(Blocks.SPRUCE_PLANKS), s(Blocks.SNOW_BLOCK), s(Blocks.SNOW_BLOCK),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.WHITE));
            case SAVANNA -> b
                    ? new HousePalette(s(Blocks.TERRACOTTA), s(Blocks.ACACIA_PLANKS), s(Blocks.STRIPPED_ACACIA_LOG),
                    s(Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW)), s(Blocks.ACACIA_PLANKS), s(Blocks.ACACIA_SLAB),
                    s(Blocks.GLASS), Blocks.ACACIA_DOOR, Blocks.BED.pick(DyeColor.LIME))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.ACACIA_PLANKS), s(Blocks.ACACIA_LOG),
                    s(Blocks.ACACIA_PLANKS), s(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE)), s(Blocks.ACACIA_SLAB),
                    s(Blocks.GLASS), Blocks.ACACIA_DOOR, Blocks.BED.pick(DyeColor.ORANGE));
            case JUNGLE -> b
                    ? new HousePalette(s(Blocks.MOSSY_COBBLESTONE), s(Blocks.BAMBOO_PLANKS), s(Blocks.STRIPPED_JUNGLE_LOG),
                    s(Blocks.BAMBOO_PLANKS), s(Blocks.BAMBOO_MOSAIC), s(Blocks.BAMBOO_MOSAIC_SLAB),
                    s(Blocks.GLASS), Blocks.BAMBOO_DOOR, Blocks.BED.pick(DyeColor.LIME))
                    : new HousePalette(s(Blocks.MOSSY_COBBLESTONE), s(Blocks.JUNGLE_PLANKS), s(Blocks.JUNGLE_LOG),
                    s(Blocks.JUNGLE_PLANKS), s(Blocks.JUNGLE_PLANKS), s(Blocks.JUNGLE_SLAB),
                    s(Blocks.GLASS), Blocks.JUNGLE_DOOR, Blocks.BED.pick(DyeColor.GREEN));
            case SWAMP -> b
                    ? new HousePalette(s(Blocks.MUD_BRICKS), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_LOG),
                    s(Blocks.MUD_BRICKS), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_SLAB),
                    s(Blocks.GLASS), Blocks.DARK_OAK_DOOR, Blocks.BED.pick(DyeColor.BROWN))
                    : new HousePalette(s(Blocks.MUD_BRICKS), s(Blocks.MANGROVE_PLANKS), s(Blocks.MANGROVE_LOG),
                    s(Blocks.MANGROVE_PLANKS), s(Blocks.MANGROVE_PLANKS), s(Blocks.MANGROVE_SLAB),
                    s(Blocks.GLASS), Blocks.MANGROVE_DOOR, Blocks.BED.pick(DyeColor.PURPLE));
            case OCEAN -> b
                    ? new HousePalette(s(Blocks.PRISMARINE), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_LOG),
                    s(Blocks.PRISMARINE_BRICKS), s(Blocks.DARK_PRISMARINE), s(Blocks.DARK_PRISMARINE_SLAB),
                    s(Blocks.GLASS), Blocks.DARK_OAK_DOOR, Blocks.BED.pick(DyeColor.CYAN))
                    : new HousePalette(s(Blocks.PRISMARINE), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_LOG),
                    s(Blocks.PRISMARINE), s(Blocks.PRISMARINE_BRICKS), s(Blocks.PRISMARINE_SLAB),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.LIGHT_BLUE));
            case CHERRY -> b
                    ? new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.CHERRY_PLANKS), s(Blocks.STRIPPED_CHERRY_LOG),
                    s(Blocks.CHERRY_PLANKS), s(Blocks.CHERRY_PLANKS), s(Blocks.CHERRY_SLAB),
                    s(Blocks.GLASS), Blocks.CHERRY_DOOR, Blocks.BED.pick(DyeColor.PINK))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.CHERRY_PLANKS), s(Blocks.CHERRY_LOG),
                    s(Blocks.CHERRY_PLANKS), s(Blocks.STRIPPED_CHERRY_LOG), s(Blocks.CHERRY_SLAB),
                    s(Blocks.GLASS), Blocks.CHERRY_DOOR, Blocks.BED.pick(DyeColor.MAGENTA));
            case BADLANDS -> b
                    ? new HousePalette(s(Blocks.RED_SANDSTONE), s(Blocks.SMOOTH_RED_SANDSTONE), s(Blocks.CUT_RED_SANDSTONE),
                    s(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE)), s(Blocks.RED_SANDSTONE), s(Blocks.RED_SANDSTONE_SLAB),
                    s(Blocks.GLASS), Blocks.ACACIA_DOOR, Blocks.BED.pick(DyeColor.ORANGE))
                    : new HousePalette(s(Blocks.RED_SANDSTONE), s(Blocks.SMOOTH_RED_SANDSTONE), s(Blocks.CUT_RED_SANDSTONE),
                    s(Blocks.TERRACOTTA), s(Blocks.SMOOTH_RED_SANDSTONE), s(Blocks.SMOOTH_RED_SANDSTONE_SLAB),
                    s(Blocks.GLASS), Blocks.ACACIA_DOOR, Blocks.BED.pick(DyeColor.BROWN));
            case MUSHROOM -> b
                    ? new HousePalette(s(Blocks.MYCELIUM), s(Blocks.BIRCH_PLANKS), s(Blocks.STRIPPED_BIRCH_LOG),
                    s(Blocks.RED_MUSHROOM_BLOCK), s(Blocks.BIRCH_PLANKS), s(Blocks.BIRCH_SLAB),
                    s(Blocks.GLASS), Blocks.BIRCH_DOOR, Blocks.BED.pick(DyeColor.RED))
                    : new HousePalette(s(Blocks.MYCELIUM), s(Blocks.BIRCH_PLANKS), s(Blocks.BIRCH_LOG),
                    s(Blocks.BROWN_MUSHROOM_BLOCK), s(Blocks.MUSHROOM_STEM), s(Blocks.BIRCH_SLAB),
                    s(Blocks.GLASS), Blocks.BIRCH_DOOR, Blocks.BED.pick(DyeColor.PURPLE));
            case DARK -> b
                    ? new HousePalette(s(Blocks.MOSSY_COBBLESTONE), s(Blocks.DARK_OAK_PLANKS), s(Blocks.STRIPPED_DARK_OAK_LOG),
                    s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_SLAB),
                    s(Blocks.GLASS), Blocks.DARK_OAK_DOOR, Blocks.BED.pick(DyeColor.BLACK))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_LOG),
                    s(Blocks.DARK_OAK_LOG), s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_SLAB),
                    s(Blocks.GLASS), Blocks.DARK_OAK_DOOR, Blocks.BED.pick(DyeColor.GRAY));
            case MOUNTAIN -> b
                    ? new HousePalette(s(Blocks.STONE_BRICKS), s(Blocks.SPRUCE_PLANKS), s(Blocks.STRIPPED_SPRUCE_LOG),
                    s(Blocks.STONE_BRICKS), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_SLAB),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.LIGHT_GRAY))
                    : new HousePalette(s(Blocks.COBBLESTONE), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_LOG),
                    s(Blocks.ANDESITE), s(Blocks.COBBLED_DEEPSLATE), s(Blocks.COBBLED_DEEPSLATE_SLAB),
                    s(Blocks.GLASS), Blocks.SPRUCE_DOOR, Blocks.BED.pick(DyeColor.BLUE));
        };
    }

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }
}
