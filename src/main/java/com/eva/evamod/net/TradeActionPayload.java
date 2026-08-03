package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TradeActionPayload(int entityId, int offerIndex) implements CustomPacketPayload {
    /** Sent when the client closes the trade UI so the NPC can resume AI. */
    public static final int ACTION_CLOSE = -1;

    public static final Type<TradeActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "trade_action"));

    public static final StreamCodec<ByteBuf, TradeActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradeActionPayload::entityId,
            ByteBufCodecs.VAR_INT, TradeActionPayload::offerIndex,
            TradeActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
