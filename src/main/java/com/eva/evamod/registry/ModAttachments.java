package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.player.PlayerEvaData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EvaMod.MODID);

    public static final Supplier<AttachmentType<PlayerEvaData>> PLAYER_DATA = ATTACHMENT_TYPES.register(
            "player_data",
            () -> AttachmentType.builder(PlayerEvaData::new)
                    .serialize(PlayerEvaData.CODEC.fieldOf("value"))
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
