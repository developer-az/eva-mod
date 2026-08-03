package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenDialoguePayload(int entityId, String npcName, String jobTitle, String line, int mood)
        implements CustomPacketPayload {

    public static final Type<OpenDialoguePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "open_dialogue"));

    public static final StreamCodec<ByteBuf, OpenDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenDialoguePayload::entityId,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::npcName,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::jobTitle,
            ByteBufCodecs.STRING_UTF8, OpenDialoguePayload::line,
            ByteBufCodecs.VAR_INT, OpenDialoguePayload::mood,
            OpenDialoguePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
