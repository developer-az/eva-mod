package com.eva.evamod.net;

import com.eva.evamod.client.ClientDialogueHandler;
import com.eva.evamod.client.ClientTradeHandler;
import com.eva.evamod.dialogue.DialogueManager;
import com.eva.evamod.entity.BiomeNpc;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientDialogueHandler.handle(payload)));

        registrar.playToClient(OpenTradePayload.TYPE, OpenTradePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientTradeHandler.handle(payload)));

        registrar.playToServer(DialogueActionPayload.TYPE, DialogueActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    if (!(player.level().getEntity(payload.entityId()) instanceof BiomeNpc npc)) {
                        return;
                    }
                    if (npc.distanceToSqr(player) > 100.0) {
                        return;
                    }
                    switch (payload.action()) {
                        case DialogueActionPayload.ACTION_TALK ->
                                npc.openDialogue(player, DialogueManager.Context.SMALL_TALK);
                        case DialogueActionPayload.ACTION_TRADE -> npc.startTrading(player);
                        default -> {
                        }
                    }
                }));

        registrar.playToServer(TradeActionPayload.TYPE, TradeActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    if (!(player.level().getEntity(payload.entityId()) instanceof BiomeNpc npc)) {
                        return;
                    }
                    npc.performCustomTrade(player, payload.offerIndex());
                }));
    }

    private ModNetworking() {
    }
}
