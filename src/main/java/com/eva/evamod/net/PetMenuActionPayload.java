package com.eva.evamod.net;

import com.eva.evamod.EvaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PetMenuActionPayload(int entityId, int action) implements CustomPacketPayload {
    public static final int ACTION_SIT_FOLLOW = 0;
    public static final int ACTION_GLOW = 1;
    public static final int ACTION_FIND = 2;
    public static final int ACTION_CHEER = 3;
    public static final int ACTION_TRINKET_HINT = 4;

    public static final Type<PetMenuActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EvaMod.MODID, "pet_menu_action"));

    public static final StreamCodec<ByteBuf, PetMenuActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PetMenuActionPayload::entityId,
            ByteBufCodecs.VAR_INT, PetMenuActionPayload::action,
            PetMenuActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
