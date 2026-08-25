package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Clientbound: open the stuffed-pet utility menu (mirrors NPC dialogue UX). */
public record OpenPetMenuPayload(
        int entityId,
        String petName,
        String kindTitle,
        String line,
        boolean sitting,
        boolean glowing,
        boolean hasTrinket) implements CustomPacketPayload {

    public static final Type<OpenPetMenuPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "open_pet_menu"));

    public static final StreamCodec<ByteBuf, OpenPetMenuPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenPetMenuPayload decode(ByteBuf buf) {
            return new OpenPetMenuPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf));
        }

        @Override
        public void encode(ByteBuf buf, OpenPetMenuPayload value) {
            ByteBufCodecs.VAR_INT.encode(buf, value.entityId());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.petName());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.kindTitle());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.line());
            ByteBufCodecs.BOOL.encode(buf, value.sitting());
            ByteBufCodecs.BOOL.encode(buf, value.glowing());
            ByteBufCodecs.BOOL.encode(buf, value.hasTrinket());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
