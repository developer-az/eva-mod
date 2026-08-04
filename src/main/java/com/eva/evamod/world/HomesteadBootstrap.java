package com.eva.evamod.world;

import com.eva.evamod.command.SettlementLocator;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.worldgen.RuntimeHomestead;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Once-per-overworld Founder's Homestead for worlds explored before Eva Mod.
 * Does not regenerate chunks; places a bounded cottage + NPC near the player.
 */
public final class HomesteadBootstrap {
    private HomesteadBootstrap() {
    }

    public static int settle(ServerPlayer player) {
        ServerLevel level = player.level();
        if (level.dimension() != ServerLevel.OVERWORLD) {
            player.sendSystemMessage(Component.literal("Founder's Homestead only works in the Overworld.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        EvaWorldData world = level.getData(ModAttachments.WORLD_DATA);
        if (world.founderHomesteadPlaced() && world.founderHomesteadPos() != null) {
            BlockPos existing = world.founderHomesteadPos();
            SettlementCache cache = level.getData(ModAttachments.SETTLEMENT_CACHE);
            cache.remember(existing, false);
            level.setData(ModAttachments.SETTLEMENT_CACHE, cache.copy());
            player.sendSystemMessage(Component.literal("This world already has a Founder's Homestead at ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(SettlementLocator.clickablePos(Component.empty(), existing)));
            player.sendSystemMessage(Component.literal("Use /evamod locate or explore new chunks for more natural houses.")
                    .withStyle(ChatFormatting.GRAY));
            return 1;
        }

        RuntimeHomestead.Result result = RuntimeHomestead.place(level, player.blockPosition());
        if (result == null) {
            player.sendSystemMessage(Component.literal(
                            "Could not find a clear build site nearby. Walk to open land (fields, not oceans or caves) and try /evamod settle again.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        world.markFounderHomestead(result.npcHome());
        world.setLikelyLegacyWorld(true);
        level.setData(ModAttachments.WORLD_DATA, world.copy());

        SettlementCache cache = level.getData(ModAttachments.SETTLEMENT_CACHE);
        cache.remember(result.npcHome(), false);
        level.setData(ModAttachments.SETTLEMENT_CACHE, cache.copy());

        BlockPos land = SettlementLocator.safeInteriorTeleportPos(level, result.npcHome(), false);
        player.sendSystemMessage(Component.literal("Founder's Homestead planted! Safe landing ")
                .withStyle(ChatFormatting.GREEN)
                .append(SettlementLocator.clickablePos(Component.empty(), land)));
        player.sendSystemMessage(Component.literal(
                        "Natural towns still generate in unexplored chunks. This starter home is once per world.")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }
}
