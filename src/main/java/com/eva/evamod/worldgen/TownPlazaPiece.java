package com.eva.evamod.worldgen;

import com.eva.evamod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Small shared plaza for npc_town clusters — paths, well, lanterns. */
public class TownPlazaPiece extends StructurePiece {
    public TownPlazaPiece(int x, int y, int z) {
        super(ModStructures.TOWN_PLAZA_PIECE.get(), 0, makeBoundingBox(x, y, z, Direction.NORTH, 11, 6, 11));
        this.setOrientation(Direction.NORTH);
    }

    public TownPlazaPiece(CompoundTag tag) {
        super(ModStructures.TOWN_PLAZA_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        this.generateBox(level, box, 0, 0, 0, 10, 4, 10, CAVE_AIR, CAVE_AIR, false);

        this.generateBox(level, box, 0, -1, 0, 10, -1, 10,
                Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), false);
        this.generateBox(level, box, 2, -1, 2, 8, -1, 8,
                Blocks.DIRT_PATH.defaultBlockState(), Blocks.DIRT_PATH.defaultBlockState(), false);

        for (int i = 0; i <= 10; i++) {
            this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), 5, -1, i, box);
            this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), i, -1, 5, box);
        }

        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), 4, 0, 4, box);
        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), 6, 0, 4, box);
        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), 4, 0, 6, box);
        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), 6, 0, 6, box);
        this.placeBlock(level, Blocks.WATER.defaultBlockState(), 5, 0, 5, box);
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 5, 1, 4, box);
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 5, 1, 6, box);
        this.placeBlock(level, Blocks.OAK_SLAB.defaultBlockState(), 5, 2, 5, box);

        placeLanternPost(level, box, 1, 0, 1);
        placeLanternPost(level, box, 9, 0, 1);
        placeLanternPost(level, box, 1, 0, 9);
        placeLanternPost(level, box, 9, 0, 9);

        this.placeBlock(level, Blocks.POPPY.defaultBlockState(), 2, 0, 2, box);
        this.placeBlock(level, Blocks.DANDELION.defaultBlockState(), 8, 0, 2, box);
        this.placeBlock(level, Blocks.AZURE_BLUET.defaultBlockState(), 2, 0, 8, box);
        this.placeBlock(level, Blocks.OXEYE_DAISY.defaultBlockState(), 8, 0, 8, box);
    }

    private void placeLanternPost(WorldGenLevel level, BoundingBox box, int x, int y, int z) {
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), x, y, z, box);
        this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), x, y + 1, z, box);
    }
}
