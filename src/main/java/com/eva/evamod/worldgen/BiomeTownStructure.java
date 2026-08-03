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
 * Uncommon multi-house hamlet: a plaza plus 3–6 biome houses arranged around it.
 * Each house still spawns its own home-bound NPC.
 */
public class BiomeTownStructure extends Structure {
    public static final MapCodec<BiomeTownStructure> CODEC = simpleCodec(BiomeTownStructure::new);

    public BiomeTownStructure(StructureSettings settings) {
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
        int houseCount = 3 + random.nextInt(4); // 3–6
        int radius = 12 + random.nextInt(4);

        BlockPos center = new BlockPos(x, surfaceY, z);
        return Optional.of(new GenerationStub(center, builder -> {
            builder.addPiece(new TownPlazaPiece(center.getX() - 5, center.getY(), center.getZ() - 5));

            // Prefer distinct layouts so the hamlet doesn't look copy-pasted.
            boolean[] usedLayouts = new boolean[BiomeHousePiece.LAYOUT_COUNT];
            for (int i = 0; i < houseCount; i++) {
                double angle = (Math.PI * 2.0 * i) / houseCount + random.nextDouble() * 0.25;
                int hx = center.getX() + (int) Math.round(Math.cos(angle) * radius);
                int hz = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
                int hy = context.chunkGenerator().getFirstOccupiedHeight(
                        hx, hz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                if (hy <= context.chunkGenerator().getSeaLevel()) {
                    hy = surfaceY;
                }

                int layout = pickLayout(random, usedLayouts);
                usedLayouts[layout] = true;
                int paletteAlt = random.nextInt(2);
                // Face roughly toward the plaza.
                Direction orientation = directionToward(hx, hz, center.getX(), center.getZ());
                builder.addPiece(new BiomeHousePiece(random, hx, hy, hz, variant, layout, paletteAlt, orientation));
            }
        }));
    }

    private static int pickLayout(WorldgenRandom random, boolean[] used) {
        int free = 0;
        for (boolean u : used) {
            if (!u) {
                free++;
            }
        }
        if (free == 0) {
            return random.nextInt(BiomeHousePiece.LAYOUT_COUNT);
        }
        int pick = random.nextInt(free);
        for (int i = 0; i < used.length; i++) {
            if (!used[i] && pick-- == 0) {
                return i;
            }
        }
        return random.nextInt(BiomeHousePiece.LAYOUT_COUNT);
    }

    private static Direction directionToward(int fromX, int fromZ, int toX, int toZ) {
        int dx = toX - fromX;
        int dz = toZ - fromZ;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.NPC_TOWN.get();
    }
}
