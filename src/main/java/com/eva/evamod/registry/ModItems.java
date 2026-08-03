package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EvaMod.MODID);

    public static final DeferredItem<Item> BIOME_NPC_SPAWN_EGG = ITEMS.registerItem("biome_npc_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.BIOME_NPC.get())));

    public static void addToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(BIOME_NPC_SPAWN_EGG.get());
        }
    }

    private ModItems() {
    }
}
