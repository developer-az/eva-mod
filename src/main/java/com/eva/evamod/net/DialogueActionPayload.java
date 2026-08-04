package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DialogueActionPayload(int entityId, int action) implements CustomPacketPayload {
    public static final int ACTION_TALK = 0;
    public static final int ACTION_TRADE = 1;
    public static final int ACTION_ERRAND = 2;
    public static final int ACTION_TIP = 3;

    public static final Type<DialogueActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "dialogue_action"));

    public static final StreamCodec<ByteBuf, DialogueActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DialogueActionPayload::entityId,
            ByteBufCodecs.VAR_INT, DialogueActionPayload::action,
            DialogueActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
