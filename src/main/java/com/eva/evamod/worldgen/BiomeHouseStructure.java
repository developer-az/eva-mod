package com.eva.evamod.worldgen;

import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Places a single NPC house on the surface. Layout, palette variation and
 * orientation are randomized; the biome at the spot decides the NPC variant
 * and block palette.
 */
public class BiomeHouseStructure extends Structure {
    public static final MapCodec<BiomeHouseStructure> CODEC = simpleCodec(BiomeHouseStructure::new);

    public BiomeHouseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (surfaceY <= context.chunkGenerator().getSeaLevel()) {
            return Optional.empty();
        }

        Holder<Biome> biome = context.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(surfaceY), QuartPos.fromBlock(z),
                context.randomState().sampler());
        NpcVariant variant = NpcVariant.fromBiome(biome);

        WorldgenRandom random = context.random();
        int layout = random.nextInt(BiomeHousePiece.LAYOUT_COUNT);
        int paletteAlt = random.nextInt(2);
        Direction orientation = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        BlockPos pos = new BlockPos(x, surfaceY, z);
        return Optional.of(new GenerationStub(pos, builder ->
                builder.addPiece(new BiomeHousePiece(random, pos.getX(), pos.getY(), pos.getZ(),
                        variant, layout, paletteAlt, orientation))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.NPC_HOUSE.get();
    }
}
