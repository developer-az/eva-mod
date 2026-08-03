package com.eva.evamod.client;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.minecraft.util.TriState;

/** Shows short speech/thought text above NPCs via the name-tag pipeline. */
@EventBusSubscriber(modid = EvaMod.MODID, value = Dist.CLIENT)
public final class NpcBubbleRenderer {

    @SubscribeEvent
    public static void onNameTag(RenderNameTagEvent.CanRender event) {
        if (!(event.getEntity() instanceof BiomeNpc npc)) {
            return;
        }
        String bubble = npc.getBubbleText();
        if (bubble != null && !bubble.isEmpty()) {
            event.setCanRender(TriState.TRUE);
            event.setContent(Component.literal("~ " + bubble));
        }
    }

    private NpcBubbleRenderer() {
    }
}
