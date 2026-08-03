package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EvaMod.MODID);

    public static final ResourceKey<EntityType<?>> BIOME_NPC_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "biome_npc"));

    public static final DeferredHolder<EntityType<?>, EntityType<BiomeNpc>> BIOME_NPC =
            ENTITY_TYPES.register("biome_npc", () -> EntityType.Builder.of(BiomeNpc::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(BIOME_NPC_KEY));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BIOME_NPC.get(), BiomeNpc.createAttributes().build());
    }

    private ModEntities() {
    }
}
