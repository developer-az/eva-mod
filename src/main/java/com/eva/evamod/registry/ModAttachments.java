package com.eva.evamod.registry;

import com.eva.evamod.EvaMod;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.world.SettlementCache;
import com.eva.evamod.world.UsedNpcNamesData;
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

    public static final Supplier<AttachmentType<UsedNpcNamesData>> USED_NPC_NAMES = ATTACHMENT_TYPES.register(
            "used_npc_names",
            () -> AttachmentType.builder(UsedNpcNamesData::new)
                    .serialize(UsedNpcNamesData.CODEC.fieldOf("value"))
                    .build());

    public static final Supplier<AttachmentType<SettlementCache>> SETTLEMENT_CACHE = ATTACHMENT_TYPES.register(
            "settlement_cache",
            () -> AttachmentType.builder(SettlementCache::new)
                    .serialize(SettlementCache.CODEC.fieldOf("value"))
                    .build());

    private ModAttachments() {
    }
}
