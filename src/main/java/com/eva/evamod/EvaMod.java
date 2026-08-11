package com.eva.evamod;

import com.eva.evamod.net.ModNetworking;
import com.eva.evamod.pet.PetBootstrap;
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
 * Eva Mod Homestead — biome NPCs with towns, hearts, seasons, errands, mail,
 * guide book, and Founder's Homestead for pre-mod worlds.
 * <p>
 * Compatibility: registry/network IDs use {@code evamod}; no global mixins;
 * schema-versioned player/world attachments migrate forward. Safe for dedicated
 * multiplayer (server authority; login work is O(1) — no structure scans on join).
 * <p>
 * Versions 1.x are outdated/unsupported. Minimum supported line: 2.0.0+.
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

        PetBootstrap.register();
    }
}
