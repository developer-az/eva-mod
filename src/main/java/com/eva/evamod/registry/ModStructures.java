package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.worldgen.BiomeHousePiece;
import com.eva.evamod.worldgen.BiomeHouseStructure;
import com.eva.evamod.worldgen.BiomeTownStructure;
import com.eva.evamod.worldgen.TownPlazaPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, EvaMod.MODID);

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, EvaMod.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<BiomeHouseStructure>> NPC_HOUSE =
            STRUCTURE_TYPES.register("npc_house", () -> () -> BiomeHouseStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<BiomeTownStructure>> NPC_TOWN =
            STRUCTURE_TYPES.register("npc_town", () -> () -> BiomeTownStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> NPC_HOUSE_PIECE =
            PIECE_TYPES.register("npc_house_piece",
                    () -> (StructurePieceType) (context, tag) -> new BiomeHousePiece(tag));

    public static final DeferredHolder<StructurePieceType, StructurePieceType> TOWN_PLAZA_PIECE =
            PIECE_TYPES.register("town_plaza_piece",
                    () -> (StructurePieceType) (context, tag) -> new TownPlazaPiece(tag));

    private ModStructures() {
    }
}
