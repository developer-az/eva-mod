package com.eva.evamod.worldgen;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.registry.ModEntities;
import com.eva.evamod.registry.ModLootTables;
import com.eva.evamod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;

/**
 * A single NPC house, built from code. Eight layouts:
 * 0 hut, 1 cottage, 2 longhouse+porch, 3 tower,
 * 4 flower cottage, 5 A-frame cabin, 6 mushroom hut, 7 stilt house.
 * Combined with two palettes per biome = 112 distinct houses.
 */
public class BiomeHousePiece extends StructurePiece {
    public static final int LAYOUT_COUNT = 8;

    private static final int[] WIDTHS =  {5, 7, 9, 5, 7, 7, 7, 7};
    private static final int[] DEPTHS =  {5, 6, 8, 5, 8, 7, 7, 7};
    private static final int[] HEIGHTS = {10, 11, 12, 15, 12, 12, 12, 14};
    private static final int[] WALL_HEIGHTS = {3, 4, 4, 7, 4, 3, 4, 4};

    private static final BlockState[] FLOWERS = {
            Blocks.POPPY.defaultBlockState(),
            Blocks.DANDELION.defaultBlockState(),
            Blocks.CORNFLOWER.defaultBlockState(),
            Blocks.AZURE_BLUET.defaultBlockState(),
            Blocks.OXEYE_DAISY.defaultBlockState(),
            Blocks.ALLIUM.defaultBlockState(),
            Blocks.LILY_OF_THE_VALLEY.defaultBlockState(),
            Blocks.BLUE_ORCHID.defaultBlockState()
    };

    private static final BlockState[] POTTED = {
            Blocks.POTTED_POPPY.defaultBlockState(),
            Blocks.POTTED_DANDELION.defaultBlockState(),
            Blocks.POTTED_CORNFLOWER.defaultBlockState(),
            Blocks.POTTED_AZURE_BLUET.defaultBlockState(),
            Blocks.POTTED_OXEYE_DAISY.defaultBlockState(),
            Blocks.POTTED_ALLIUM.defaultBlockState(),
            Blocks.POTTED_LILY_OF_THE_VALLEY.defaultBlockState(),
            Blocks.POTTED_RED_TULIP.defaultBlockState()
    };

    private static final BlockState[] CARPETS = {
            Blocks.CARPET.pick(DyeColor.RED).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.ORANGE).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.YELLOW).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.LIME).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.PINK).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.WHITE).defaultBlockState(),
            Blocks.CARPET.pick(DyeColor.PURPLE).defaultBlockState()
    };

    private final int variantId;
    private final int layout;
    private final int paletteAlt;
    private boolean spawnedNpc;

    public BiomeHousePiece(RandomSource random, int x, int y, int z,
                           NpcVariant variant, int layout, int paletteAlt, Direction orientation) {
        // +4 on width/depth leaves a 2-block apron of gardens/path around the house
        super(ModStructures.NPC_HOUSE_PIECE.get(), 0,
                makeBoundingBox(x, y, z, orientation, WIDTHS[layout] + 4, HEIGHTS[layout], DEPTHS[layout] + 4));
        this.setOrientation(orientation);
        this.variantId = variant.ordinal();
        this.layout = layout;
        this.paletteAlt = paletteAlt;
    }

    public BiomeHousePiece(CompoundTag tag) {
        super(ModStructures.NPC_HOUSE_PIECE.get(), tag);
        this.variantId = tag.getIntOr("NpcVariant", 0);
        this.layout = Math.floorMod(tag.getIntOr("Layout", 0), LAYOUT_COUNT);
        this.paletteAlt = tag.getIntOr("PaletteAlt", 0);
        this.spawnedNpc = tag.getBooleanOr("SpawnedNpc", false);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("NpcVariant", variantId);
        tag.putInt("Layout", layout);
        tag.putInt("PaletteAlt", paletteAlt);
        tag.putBoolean("SpawnedNpc", spawnedNpc);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        NpcVariant variant = NpcVariant.byId(variantId);
        HousePalette palette = HousePalette.of(variant, paletteAlt);
        NpcJob job = variant.randomJob(random);

        // Local house coordinates are offset by +2/+2 inside the bounding box
        // so gardens and the path sit at 0..width+3 / 0..depth+3
        int ox = 2;
        int oz = 2;
        int width = WIDTHS[layout];
        int depth = DEPTHS[layout];
        int wallHeight = WALL_HEIGHTS[layout];
        int floorY = layout == 7 ? 3 : 0; // stilt house sits on stilts
        int frontWall = layout == 2 ? 2 : 0;

        // Clear the whole site and plant a soft dirt apron.
        // Foundation fill is depth-capped — full fillColumnDown to bedrock was a major lag source.
        this.generateBox(level, box, 0, floorY, 0, width + 3, floorY + wallHeight + 6, depth + 3,
                CAVE_AIR, CAVE_AIR, false);
        for (int x = 0; x < width + 4; x++) {
            for (int z = 0; z < depth + 4; z++) {
                this.placeBlock(level, Blocks.GRASS_BLOCK.defaultBlockState(), x, floorY - 1, z, box);
                this.fillFoundation(level, palette.foundation(), x, floorY - 2, z, box, 4);
            }
        }

        // Front path of dirt / gravel leading to the door
        int doorX = ox + width / 2;
        for (int z = 0; z <= oz + frontWall; z++) {
            this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), doorX, floorY - 1, z, box);
            if (z > 0) {
                this.placeBlock(level, Blocks.DIRT_PATH.defaultBlockState(), doorX - 1, floorY - 1, z, box);
            }
        }

        // Floor
        this.generateBox(level, box, ox, floorY, oz, ox + width - 1, floorY, oz + depth - 1,
                palette.floor(), palette.floor(), false);
        for (int x = ox; x < ox + width; x++) {
            for (int z = oz; z < oz + depth; z++) {
                this.fillFoundation(level, palette.foundation(), x, floorY - 1, z, box, 5);
            }
        }

        // Stilts under the house for layout 7
        if (layout == 7) {
            for (int y = 0; y < floorY; y++) {
                this.placeBlock(level, palette.post(), ox, y, oz, box);
                this.placeBlock(level, palette.post(), ox + width - 1, y, oz, box);
                this.placeBlock(level, palette.post(), ox, y, oz + depth - 1, box);
                this.placeBlock(level, palette.post(), ox + width - 1, y, oz + depth - 1, box);
                this.placeBlock(level, palette.post(), ox + width / 2, y, oz, box);
                this.placeBlock(level, palette.post(), ox + width / 2, y, oz + depth - 1, box);
            }
            // Ladder up the front
            BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            for (int y = 0; y <= floorY; y++) {
                this.placeBlock(level, ladder, doorX, y, oz - 1, box);
            }
        }

        // Clear interior
        this.generateBox(level, box, ox + 1, floorY + 1, oz + frontWall + 1,
                ox + width - 2, floorY + wallHeight, oz + depth - 2, CAVE_AIR, CAVE_AIR, false);
        if (frontWall > 0) {
            this.generateBox(level, box, ox, floorY + 1, oz, ox + width - 1, floorY + wallHeight, oz + frontWall - 1,
                    CAVE_AIR, CAVE_AIR, false);
        }

        // Walls
        this.generateBox(level, box, ox, floorY + 1, oz + frontWall, ox + width - 1, floorY + wallHeight, oz + frontWall,
                palette.wall(), palette.wall(), false);
        this.generateBox(level, box, ox, floorY + 1, oz + depth - 1, ox + width - 1, floorY + wallHeight, oz + depth - 1,
                palette.wall(), palette.wall(), false);
        this.generateBox(level, box, ox, floorY + 1, oz + frontWall, ox, floorY + wallHeight, oz + depth - 1,
                palette.wall(), palette.wall(), false);
        this.generateBox(level, box, ox + width - 1, floorY + 1, oz + frontWall, ox + width - 1, floorY + wallHeight,
                oz + depth - 1, palette.wall(), palette.wall(), false);

        // Corner posts
        for (int y = floorY + 1; y <= floorY + wallHeight; y++) {
            this.placeBlock(level, palette.post(), ox, y, oz + frontWall, box);
            this.placeBlock(level, palette.post(), ox + width - 1, y, oz + frontWall, box);
            this.placeBlock(level, palette.post(), ox, y, oz + depth - 1, box);
            this.placeBlock(level, palette.post(), ox + width - 1, y, oz + depth - 1, box);
        }
        if (frontWall > 0) {
            for (int y = floorY + 1; y <= floorY + wallHeight; y++) {
                this.placeBlock(level, palette.post(), ox, y, oz, box);
                this.placeBlock(level, palette.post(), ox + width - 1, y, oz, box);
            }
            // Porch railing
            this.generateBox(level, box, ox + 1, floorY + 1, oz, ox + width - 2, floorY + 1, oz,
                    Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), false);
            this.placeBlock(level, CAVE_AIR, doorX, floorY + 1, oz, box);
        }

        // Windows with cute trapdoor shutters
        int winZ1 = oz + frontWall + 2;
        int winZ2 = oz + depth - 3;
        placeWindow(level, box, ox, floorY + 2, winZ1, Direction.WEST, random);
        placeWindow(level, box, ox + width - 1, floorY + 2, winZ1, Direction.EAST, random);
        if (winZ2 > winZ1) {
            placeWindow(level, box, ox, floorY + 2, winZ2, Direction.WEST, random);
            placeWindow(level, box, ox + width - 1, floorY + 2, winZ2, Direction.EAST, random);
        }
        placeWindow(level, box, ox + width / 2, floorY + 2, oz + depth - 1, Direction.SOUTH, random);

        // Door — spawn open so pathfinding can enter; keep a 2-block-high clear corridor.
        int doorZ = oz + frontWall;
        BlockState doorLower = palette.door().defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, true);
        clearDoorCorridor(level, box, doorX, floorY, doorZ);
        this.placeBlock(level, doorLower, doorX, floorY + 1, doorZ, box);
        this.placeBlock(level, doorLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                doorX, floorY + 2, doorZ, box);

        // Chimney with a cozy campfire
        int chimneyX = ox + width - 2;
        int chimneyZ = oz + depth - 2;
        int chimneyBase = floorY + wallHeight + 1;
        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), chimneyX, chimneyBase, chimneyZ, box);
        this.placeBlock(level, Blocks.COBBLESTONE.defaultBlockState(), chimneyX, chimneyBase + 1, chimneyZ, box);
        this.placeBlock(level, Blocks.CAMPFIRE.defaultBlockState(), chimneyX, chimneyBase + 2, chimneyZ, box);

        // Layout-specific roof + extras
        switch (layout) {
            case 3 -> buildTowerExtras(level, box, ox, oz, width, depth, floorY, palette);
            case 4 -> {
                buildFlowerGarden(level, box, ox, oz, width, depth, floorY, random);
                buildSteppedRoof(level, box, ox, oz, width, depth, floorY, wallHeight, palette);
            }
            case 5 -> buildAFrameRoof(level, box, ox, oz, width, depth, floorY, wallHeight, palette);
            case 6 -> buildMushroomExtras(level, box, ox, oz, width, depth, floorY, wallHeight, random);
            default -> buildSteppedRoof(level, box, ox, oz, width, depth, floorY, wallHeight, palette);
        }

        // Interior furnishings — place bed only when BOTH parts are inside this chunk box
        // (half-beds on chunk borders break into items on neighbor update).
        int interiorBackZ = oz + depth - 2;
        int interiorFrontZ = oz + frontWall + 1;
        placeBedPair(level, box, palette, ox + 1, floorY + 1, interiorBackZ);

        this.placeBlock(level, job.getJobBlock().defaultBlockState(), ox + width - 2, floorY + 1, interiorFrontZ, box);
        this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), ox + 1, floorY + 1, interiorFrontZ, box);
        ResourceKey<LootTable> loot = random.nextBoolean()
                ? ModLootTables.forVariant(variant)
                : ModLootTables.forJob(job);
        this.createChest(level, box, random, ox + width - 2, floorY + 1, interiorBackZ, loot);

        // Carpet runner — never on the door threshold or first step inside (pathfinding + door).
        BlockState carpet = CARPETS[random.nextInt(CARPETS.length)];
        int carpetStartZ = Math.max(interiorFrontZ + 1, doorZ + 2);
        for (int z = carpetStartZ; z <= interiorBackZ; z++) {
            if (z == interiorBackZ) {
                continue; // keep bed clear
            }
            this.placeBlock(level, carpet, doorX, floorY + 1, z, box);
        }
        // Re-place bed after carpet pass (still only if both halves fit).
        placeBedPair(level, box, palette, ox + 1, floorY + 1, interiorBackZ);
        // Re-clear doorway corridor after furniture passes (2 blocks high, in front + threshold + first step).
        clearDoorCorridor(level, box, doorX, floorY, doorZ);
        this.placeBlock(level, doorLower, doorX, floorY + 1, doorZ, box);
        this.placeBlock(level, doorLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                doorX, floorY + 2, doorZ, box);

        // Fence lantern posts by the path (keep door approach clear)
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), doorX - 2, floorY, oz - 1, box);
        this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), doorX - 2, floorY + 1, oz - 1, box);
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), doorX + 2, floorY, oz - 1, box);
        this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), doorX + 2, floorY + 1, oz - 1, box);

        // Little flower patches by the walls (not for stilts - they have no ground garden)
        if (layout != 7) {
            placeFlowerPatch(level, box, ox - 1, floorY, oz + 1, random);
            placeFlowerPatch(level, box, ox + width, floorY, oz + 1, random);
            placeFlowerPatch(level, box, ox - 1, floorY, oz + depth - 2, random);
            // Potted plant beside the path, never in the doorway column
            this.placeBlock(level, POTTED[random.nextInt(POTTED.length)], doorX + 2, floorY, oz + frontWall - 1, box);
        }

        // Tiny garden fence for flower cottage / cottage / hut
        if (layout == 0 || layout == 1 || layout == 4) {
            buildFrontGarden(level, box, ox, oz, width, floorY, random);
        }

        this.spawnNpc(level, box, ox, oz, width, depth, floorY, frontWall, job);
    }

    /** Clears a 2-tall air column in front of, at, and just inside the door for pathfinding. */
    private void clearDoorCorridor(WorldGenLevel level, BoundingBox box, int doorX, int floorY, int doorZ) {
        for (int dz = -1; dz <= 1; dz++) {
            this.placeBlock(level, CAVE_AIR, doorX, floorY + 1, doorZ + dz, box);
            this.placeBlock(level, CAVE_AIR, doorX, floorY + 2, doorZ + dz, box);
        }
    }

    private void placeWindow(WorldGenLevel level, BoundingBox box, int x, int y, int z,
                             Direction outward, RandomSource random) {
        this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), x, y, z, box);
        // Trapdoor shutter on the outside face
        BlockPos out = new BlockPos(x, y, z).relative(outward);
        BlockState shutter = Blocks.OAK_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, outward)
                .setValue(TrapDoorBlock.HALF, Half.BOTTOM)
                .setValue(TrapDoorBlock.OPEN, true);
        this.placeBlock(level, shutter, out.getX(), out.getY(), out.getZ(), box);
        // Little wooden lip under the window as a flower box
        this.placeBlock(level, Blocks.OAK_SLAB.defaultBlockState(), out.getX(), out.getY() - 1, out.getZ(), box);
    }

    private void buildSteppedRoof(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                  int width, int depth, int floorY, int wallHeight, HousePalette palette) {
        int roofBase = floorY + wallHeight + 1;
        int step = 0;
        while (true) {
            int x1 = ox + step;
            int z1 = oz + step;
            int x2 = ox + width - 1 - step;
            int z2 = oz + depth - 1 - step;
            if (x1 > x2 || z1 > z2) {
                break;
            }
            boolean lastLayer = x1 == x2 || z1 == z2 || (x2 - x1 <= 1 && z2 - z1 <= 1);
            BlockState state = lastLayer ? palette.roofCap() : palette.roof();
            this.generateBox(level, box, x1, roofBase + step, z1, x2, roofBase + step, z2, state, state, false);
            if (lastLayer) {
                break;
            }
            step++;
        }
    }

    private void buildAFrameRoof(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                 int width, int depth, int floorY, int wallHeight, HousePalette palette) {
        // Tall peaked A-frame: each step inward raises a roof layer
        int base = floorY + wallHeight;
        int half = width / 2;
        for (int step = 0; step <= half; step++) {
            int x1 = ox + step;
            int x2 = ox + width - 1 - step;
            int y = base + step;
            this.generateBox(level, box, x1, y, oz, x2, y, oz + depth - 1, palette.roof(), palette.roof(), false);
            if (x1 == x2) {
                this.generateBox(level, box, x1, y, oz, x2, y, oz + depth - 1, palette.roofCap(), palette.roofCap(), false);
            }
        }
        // Peekaboo skylight
        this.placeBlock(level, Blocks.GLASS.defaultBlockState(), ox + half, base + half - 1, oz + depth / 2, box);
    }

    private void buildTowerExtras(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                  int width, int depth, int floorY, HousePalette palette) {
        // Second story floor with a ladder hole
        this.generateBox(level, box, ox + 1, floorY + 4, oz + 1, ox + width - 2, floorY + 4, oz + depth - 2,
                palette.floor(), palette.floor(), false);
        this.placeBlock(level, CAVE_AIR, ox + 1, floorY + 4, oz + 1, box);
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floorY + 1; y <= floorY + 4; y++) {
            this.placeBlock(level, ladder, ox + 1, y, oz + 1, box);
        }
        this.placeBlock(level, Blocks.LANTERN.defaultBlockState(), ox + 3, floorY + 5, oz + 3, box);
        // Upper windows
        this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), ox, floorY + 6, oz + 2, box);
        this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), ox + width - 1, floorY + 6, oz + 2, box);
        this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), ox + width / 2, floorY + 6, oz, box);
        // Flag on top
        this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), ox + width / 2, floorY + 9, oz + depth / 2, box);
        this.placeBlock(level, Blocks.BANNER.pick(DyeColor.RED).defaultBlockState(), ox + width / 2, floorY + 10, oz + depth / 2, box);
        buildSteppedRoof(level, box, ox, oz, width, depth, floorY, 7, palette);
    }

    private void buildFlowerGarden(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                   int width, int depth, int floorY, RandomSource random) {
        // Raised flower beds along both sides, fenced
        for (int z = oz + 1; z < oz + depth - 1; z++) {
            this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), ox - 1, floorY, z, box);
            this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], ox - 1, floorY + 1, z, box);
            this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), ox + width, floorY, z, box);
            this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], ox + width, floorY + 1, z, box);
        }
        // Bee nest chance for extra cuteness
        if (random.nextInt(3) == 0) {
            this.placeBlock(level, Blocks.BEE_NEST.defaultBlockState(), ox + width / 2, floorY + 5, oz + depth - 1, box);
        }
    }

    private void buildMushroomExtras(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                     int width, int depth, int floorY, int wallHeight, RandomSource random) {
        // Overgrown mushroom roof instead of normal tiles
        int roofBase = floorY + wallHeight + 1;
        BlockState cap = random.nextBoolean()
                ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
        // MC 26.2: mushroom_stem is not a RotatedPillarBlock — no AXIS property.
        BlockState stem = Blocks.MUSHROOM_STEM.defaultBlockState();
        // Stem posts at corners poking out of the roof
        this.placeBlock(level, stem, ox, roofBase, oz, box);
        this.placeBlock(level, stem, ox + width - 1, roofBase, oz, box);
        this.placeBlock(level, stem, ox, roofBase, oz + depth - 1, box);
        this.placeBlock(level, stem, ox + width - 1, roofBase, oz + depth - 1, box);
        // Domed mushroom cap
        for (int step = 0; step < 3; step++) {
            int x1 = ox + step;
            int z1 = oz + step;
            int x2 = ox + width - 1 - step;
            int z2 = oz + depth - 1 - step;
            this.generateBox(level, box, x1, roofBase + step, z1, x2, roofBase + step, z2, cap, cap, false);
        }
        // Tiny mushrooms around the base
        this.placeBlock(level, Blocks.RED_MUSHROOM.defaultBlockState(), ox - 1, floorY, oz + 2, box);
        this.placeBlock(level, Blocks.BROWN_MUSHROOM.defaultBlockState(), ox + width, floorY, oz + 3, box);
        this.placeBlock(level, Blocks.RED_MUSHROOM.defaultBlockState(), ox + 2, floorY, oz + depth, box);
    }

    private void buildFrontGarden(WorldGenLevel level, BoundingBox box, int ox, int oz,
                                  int width, int floorY, RandomSource random) {
        // Two little fence-lined beds flanking the path
        for (int x = ox; x < ox + 2; x++) {
            this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), x, floorY, oz - 1, box);
            this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], x, floorY + 1, oz - 1, box);
        }
        for (int x = ox + width - 2; x < ox + width; x++) {
            this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), x, floorY, oz - 1, box);
            this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], x, floorY + 1, oz - 1, box);
        }
    }

    private void placeFlowerPatch(WorldGenLevel level, BoundingBox box, int x, int y, int z, RandomSource random) {
        this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], x, y, z, box);
        if (random.nextBoolean()) {
            this.placeBlock(level, FLOWERS[random.nextInt(FLOWERS.length)], x, y, z + 1, box);
        }
    }

    /** Shallow foundation support — avoids filling entire columns to bedrock. */
    private void fillFoundation(WorldGenLevel level, BlockState state, int x, int startY, int z,
                                BoundingBox box, int maxDepth) {
        for (int d = 0; d < maxDepth; d++) {
            int y = startY - d;
            BlockPos worldPos = new BlockPos(this.getWorldX(x, z), this.getWorldY(y), this.getWorldZ(x, z));
            if (!box.isInside(worldPos)) {
                return;
            }
            BlockState existing = level.getBlockState(worldPos);
            if (!existing.isAir() && !existing.liquid() && existing.getDestroySpeed(level, worldPos) >= 0
                    && !existing.canBeReplaced()) {
                return;
            }
            this.placeBlock(level, state, x, y, z, box);
        }
    }

    private void spawnNpc(WorldGenLevel level, BoundingBox box, int ox, int oz,
                           int width, int depth, int floorY, int frontWall, NpcJob job) {
        if (spawnedNpc) {
            return;
        }
        int cx = ox + width / 2;
        int cz = (oz + frontWall + 1 + oz + depth - 2) / 2;
        BlockPos spawnPos = new BlockPos(this.getWorldX(cx, cz), this.getWorldY(floorY + 1), this.getWorldZ(cx, cz));
        if (!box.isInside(spawnPos)) {
            return;
        }
        this.spawnedNpc = true;
        BiomeNpc npc = ModEntities.BIOME_NPC.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (npc == null) {
            return;
        }
        npc.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
        npc.setHomePos(spawnPos);
        npc.setVariant(NpcVariant.byId(variantId));
        npc.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.STRUCTURE, null);
        npc.setVariant(NpcVariant.byId(variantId));
        npc.setJob(job);
        level.addFreshEntityWithPassengers(npc);
    }

    /**
     * Places foot+head together only when both world positions are inside {@code box}.
     * Partial placement across chunk borders is the classic "bed pops into items" bug.
     */
    private void placeBedPair(WorldGenLevel level, BoundingBox box, HousePalette palette,
                              int footX, int y, int z) {
        BlockPos footPos = new BlockPos(this.getWorldX(footX, z), this.getWorldY(y), this.getWorldZ(footX, z));
        BlockPos headPos = new BlockPos(this.getWorldX(footX + 1, z), this.getWorldY(y), this.getWorldZ(footX + 1, z));
        if (!box.isInside(footPos) || !box.isInside(headPos)) {
            return;
        }
        BlockState bedFoot = palette.bed().defaultBlockState()
                .setValue(BedBlock.PART, BedPart.FOOT).setValue(BedBlock.FACING, Direction.EAST);
        BlockState bedHead = palette.bed().defaultBlockState()
                .setValue(BedBlock.PART, BedPart.HEAD).setValue(BedBlock.FACING, Direction.EAST);
        this.placeBlock(level, bedFoot, footX, y, z, box);
        this.placeBlock(level, bedHead, footX + 1, y, z, box);
    }
}
