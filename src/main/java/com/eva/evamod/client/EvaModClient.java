package com.eva.evamod.client;

import com.eva.evamod.EvaMod;
import com.eva.evamod.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = EvaMod.MODID, value = Dist.CLIENT)
public final class EvaModClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BIOME_NPC.get(), NpcRenderer::new);
    }

    private EvaModClient() {
    }
}
