package com.eva.evamod.player;

import com.eva.evamod.EvaMod;
import com.eva.evamod.compat.DataMigrations;
import com.eva.evamod.mail.MailService;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.world.EvaWorldData;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Login bootstrap — schema migrate, one-time guide book, light tips.
 * Intentionally avoids structure searches / worldgen on join (MP-safe).
 */
@EventBusSubscriber(modid = EvaMod.MODID)
public final class PlayerJoinHandler {
    private static final Set<UUID> TIPPED_THIS_SESSION = ConcurrentHashMap.newKeySet();

    private PlayerJoinHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DataMigrations.ensurePlayer(player);
        ServerLevel overworld = player.level().getServer().overworld();
        EvaWorldData world = DataMigrations.ensureWorld(overworld);

        // Once-per-player primer — works for brand-new and already-started worlds.
        GuideBookService.tryGiveOnce(player);

        // Day-gated mail only (no scans).
        MailService.tryDeliver(player);

        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        com.eva.evamod.adventure.AdventureService.ensureStarted(player, data);
        data = player.getData(ModAttachments.PLAYER_DATA);

        if (!TIPPED_THIS_SESSION.add(player.getUUID())) {
            return;
        }

        MutableComponent tip = Component.literal("Eva Mod — open your ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Homestead Primer").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" book, or type ").withStyle(ChatFormatting.GRAY))
                .append(suggest("/evamod", "Help"))
                .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                .append(suggest("/evamod adventure", "Stories"));
        player.sendSystemMessage(tip);

        if (world.likelyLegacyWorld() && !world.founderHomesteadPlaced()) {
            player.sendSystemMessage(Component.literal(
                            "This world looks pre-explored. Natural houses need new chunks — or plant one with ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(suggest("/evamod settle", "Founder's Homestead once per world")));
        }

        int unread = data.unreadMailCount();
        if (unread > 0) {
            player.sendSystemMessage(Component.literal("You have " + unread + " unread letter(s). ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(suggest("/evamod mail", "Read mail")));
        }
    }

    private static MutableComponent suggest(String command, String hover) {
        return Component.literal(command).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
    }
}
