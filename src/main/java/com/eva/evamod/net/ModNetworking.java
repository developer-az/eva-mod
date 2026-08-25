package com.eva.evamod.net;

import com.eva.evamod.client.ClientDialogueHandler;
import com.eva.evamod.client.ClientPetMenuHandler;
import com.eva.evamod.client.ClientTradeHandler;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.StuffedPet;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Integer.toString(com.eva.evamod.ModVersions.NETWORK));

        registrar.playToClient(OpenDialoguePayload.TYPE, OpenDialoguePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientDialogueHandler.handle(payload)));

        registrar.playToClient(OpenTradePayload.TYPE, OpenTradePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientTradeHandler.handle(payload)));

        registrar.playToClient(OpenPetMenuPayload.TYPE, OpenPetMenuPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPetMenuHandler.handle(payload)));

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
                    npc.handleDialogueAction(player, payload.action());
                }));

        registrar.playToServer(PetMenuActionPayload.TYPE, PetMenuActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    if (!(player.level().getEntity(payload.entityId()) instanceof StuffedPet pet)) {
                        return;
                    }
                    if (pet.distanceToSqr(player) > 100.0 || !pet.isOwnedBy(player)) {
                        return;
                    }
                    pet.handleMenuAction(player, payload.action());
                }));

        registrar.playToServer(TradeActionPayload.TYPE, TradeActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    if (!(player.level().getEntity(payload.entityId()) instanceof BiomeNpc npc)) {
                        return;
                    }
                    if (payload.offerIndex() == TradeActionPayload.ACTION_CLOSE) {
                        if (npc.getTradingPlayer() == player) {
                            npc.stopTrading();
                        }
                        return;
                    }
                    npc.performCustomTrade(player, payload.offerIndex());
                }));
    }

    private ModNetworking() {
    }
}
