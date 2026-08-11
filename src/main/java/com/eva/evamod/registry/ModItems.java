package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.item.AlivePlushItem;
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

    public static final DeferredItem<Item> ALIVE_PLUSH = ITEMS.registerItem("alive_plush",
            props -> new AlivePlushItem(props.stacksTo(16)));

    public static final DeferredItem<Item> STUFFED_PET_SPAWN_EGG = ITEMS.registerItem("stuffed_pet_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.STUFFED_PET.get())));

    public static void addToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(BIOME_NPC_SPAWN_EGG.get());
            event.accept(STUFFED_PET_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ALIVE_PLUSH.get());
        }
    }

    private ModItems() {
    }
}
