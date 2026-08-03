package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record OpenTradePayload(int entityId, String npcName, List<TradeRow> rows) implements CustomPacketPayload {
    public record TradeRow(ItemStack cost, ItemStack result, int uses, int maxUses) {
        public static final StreamCodec<RegistryFriendlyByteBuf, TradeRow> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, TradeRow::cost,
                ItemStack.OPTIONAL_STREAM_CODEC, TradeRow::result,
                ByteBufCodecs.VAR_INT, TradeRow::uses,
                ByteBufCodecs.VAR_INT, TradeRow::maxUses,
                TradeRow::new);
    }

    public static final Type<OpenTradePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "open_trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTradePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenTradePayload::entityId,
            ByteBufCodecs.STRING_UTF8, OpenTradePayload::npcName,
            ByteBufCodecs.collection(ArrayList::new, TradeRow.STREAM_CODEC), OpenTradePayload::rows,
            OpenTradePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
