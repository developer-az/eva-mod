package com.eva.evamod;

import com.eva.evamod.net.ModNetworking;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.registry.ModEntities;
import com.eva.evamod.registry.ModItems;
import com.eva.evamod.registry.ModStructures;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Eva Mod 2.0 Homestead — biome NPCs with towns, hearts, seasons, errands, and mail.
 * <p>
 * Compatibility notes: all registries/network IDs use {@code evamod}; no global mixins;
 * structure set salt is unique; entity attributes via NeoForge events. Safe for dedicated
 * multiplayer (server authority for dialogue/trades/memory; client only renders UI/bubbles).
 * <p>
 * Versions 1.x are outdated/unsupported.
 */
@Mod(EvaMod.MODID)
public class EvaMod {
    public static final String MODID = "evamod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EvaMod(IEventBus modEventBus, ModContainer container) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.PIECE_TYPES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModItems::addToTabs);
        modEventBus.addListener(ModNetworking::register);
    }
}
