package com.eva.evamod.worldgen;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Tiny runtime cottage for pre-mod / fully-explored worlds.
 * Bounded placement (~9×7×6) — no chunk regen, no structure search, MP-friendly.
 */
public final class RuntimeHomestead {
    private static final int WIDTH = 7;
    private static final int DEPTH = 6;
    private static final int WALL = 3;

    private RuntimeHomestead() {
    }

    public record Result(BlockPos doorstep, BlockPos bedPos, BlockPos npcHome) {
    }

    /**
     * Find a reasonable build site near {@code origin}. Cheap spiral + heightmap only.
     * Returns null if nothing suitable within the search budget.
     */
    public static BlockPos findSite(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        // Prefer a ring away from the player so we don't overwrite bases at their feet.
        int[] radii = {48, 56, 64, 40, 72, 80};
        for (int radius : radii) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
                level.getChunkAt(new BlockPos(x, 64, z));
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y < level.getSeaLevel() + 1 || y > level.getMaxY() - 16) {
                    continue;
                }
                BlockPos feet = new BlockPos(x, y, z);
                if (!isBuildable(level, feet)) {
                    continue;
                }
                return feet;
            }
        }
        return null;
    }

    private static boolean isBuildable(ServerLevel level, BlockPos feet) {
        BlockState ground = level.getBlockState(feet.below());
        if (ground.isAir() || ground.liquid() || ground.is(Blocks.BEDROCK)) {
            return false;
        }
        // Avoid packing a house into someone's existing room (too many solid blocks in volume).
        int solids = 0;
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dz = 0; dz < DEPTH; dz++) {
                for (int dy = 0; dy <= WALL + 2; dy++) {
                    BlockState state = level.getBlockState(feet.offset(dx, dy, dz));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        solids++;
                        if (solids > 12) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Result place(ServerLevel level, BlockPos origin) {
        BlockPos site = findSite(level, origin);
        if (site == null) {
            return null;
        }
        RandomSource random = level.getRandom();
        NpcVariant variant = NpcVariant.fromBiome(level.getBiome(site));
        HousePalette palette = HousePalette.of(variant, random.nextInt(2));
        NpcJob job = variant.randomJob(random);

        // Floor + clear interior
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                BlockPos ground = site.offset(x, -1, z);
                level.setBlock(ground, palette.foundation(), 2);
                level.setBlock(site.offset(x, 0, z), palette.floor(), 2);
                for (int y = 1; y <= WALL + 2; y++) {
                    level.setBlock(site.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // Walls
        for (int y = 1; y <= WALL; y++) {
            for (int x = 0; x < WIDTH; x++) {
                level.setBlock(site.offset(x, y, 0), palette.wall(), 2);
                level.setBlock(site.offset(x, y, DEPTH - 1), palette.wall(), 2);
            }
            for (int z = 1; z < DEPTH - 1; z++) {
                level.setBlock(site.offset(0, y, z), palette.wall(), 2);
                level.setBlock(site.offset(WIDTH - 1, y, z), palette.wall(), 2);
            }
        }

        // Door (south-facing front at z=0 center)
        int doorX = WIDTH / 2;
        BlockState doorLower = palette.door().defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.OPEN, true);
        BlockState doorUpper = palette.door().defaultBlockState()
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.OPEN, true);
        level.setBlock(site.offset(doorX, 1, 0), doorLower, 2);
        level.setBlock(site.offset(doorX, 2, 0), doorUpper, 2);

        // Simple roof slab line
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                level.setBlock(site.offset(x, WALL + 1, z), palette.roof(), 2);
            }
        }

        // Bed + lantern + job block
        BlockPos bedFoot = site.offset(1, 1, DEPTH - 2);
        BlockPos bedHead = site.offset(2, 1, DEPTH - 2);
        BlockState foot = palette.bed().defaultBlockState()
                .setValue(BedBlock.PART, BedPart.FOOT).setValue(BedBlock.FACING, Direction.EAST);
        BlockState head = palette.bed().defaultBlockState()
                .setValue(BedBlock.PART, BedPart.HEAD).setValue(BedBlock.FACING, Direction.EAST);
        level.setBlock(bedFoot, foot, 2);
        level.setBlock(bedHead, head, 2);
        level.setBlock(site.offset(WIDTH - 2, 1, 1), job.getJobBlock().defaultBlockState(), 2);
        level.setBlock(site.offset(1, 1, 1), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(site.offset(doorX, 1, 1), Blocks.CARPET.pick(DyeColor.RED).defaultBlockState(), 2);

        BlockPos npcHome = site.offset(doorX, 1, DEPTH / 2);
        BiomeNpc npc = ModEntities.BIOME_NPC.get().create(level, EntitySpawnReason.COMMAND);
        if (npc != null) {
            npc.snapTo(npcHome.getX() + 0.5, npcHome.getY(), npcHome.getZ() + 0.5, 0.0F, 0.0F);
            npc.setHomePos(npcHome);
            npc.setVariant(variant);
            npc.finalizeSpawn(level, level.getCurrentDifficultyAt(npcHome), EntitySpawnReason.COMMAND, null);
            npc.setVariant(variant);
            npc.setJob(job);
            level.addFreshEntityWithPassengers(npc);
        }

        BlockPos doorstep = site.offset(doorX, 1, 0);
        return new Result(doorstep, bedFoot, npcHome);
    }
}
