package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenDialoguePayload(
        int entityId,
        String npcName,
        String jobTitle,
        String line,
        int mood,
        int hearts,
        String birthday,
        boolean canErrand) implements CustomPacketPayload {

    public static final Type<OpenDialoguePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "open_dialogue"));

    public static final StreamCodec<ByteBuf, OpenDialoguePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenDialoguePayload decode(ByteBuf buf) {
            return new OpenDialoguePayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf));
        }

        @Override
        public void encode(ByteBuf buf, OpenDialoguePayload value) {
            ByteBufCodecs.VAR_INT.encode(buf, value.entityId());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.npcName());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.jobTitle());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.line());
            ByteBufCodecs.VAR_INT.encode(buf, value.mood());
            ByteBufCodecs.VAR_INT.encode(buf, value.hearts());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.birthday());
            ByteBufCodecs.BOOL.encode(buf, value.canErrand());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
