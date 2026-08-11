package com.eva.evamod.adventure;

import com.eva.evamod.mail.MailMessage;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Advances adventure steps from existing gameplay hooks.
 * Never allocates per tick — only on player actions / commands.
 */
public final class AdventureService {
    private AdventureService() {
    }

    public enum Signal {
        MEET_NPC,
        COMPLETE_ERRAND,
        AWAKEN_PET,
        PET_GLOW,
        PET_FIND,
        LOCATE,
        DISCOVER_TOWN,
        OPEN_JOURNAL,
        READ_MAIL,
        GIFT_NPC,
        CHECK_CALENDAR
    }

    /** Ensure starter adventures exist; auto-start first_steps + soft_companion. */
    public static void ensureStarted(ServerPlayer player, PlayerEvaData data) {
        boolean dirty = false;
        if (data.findAdventure("first_steps") == null) {
            data.upsertAdventure(new AdventureProgress("first_steps", 0, false, 0));
            dirty = true;
        }
        if (data.findAdventure("soft_companion") == null) {
            data.upsertAdventure(new AdventureProgress("soft_companion", 0, false, 0));
            dirty = true;
        }
        if (dirty) {
            player.setData(ModAttachments.PLAYER_DATA, data.copy());
        }
    }

    public static void signal(ServerPlayer player, Signal signal) {
        signal(player, signal, null);
    }

    public static void signal(ServerPlayer player, Signal signal, String detail) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        ensureStarted(player, data);
        data = player.getData(ModAttachments.PLAYER_DATA);

        List<String> completedNow = new ArrayList<>();
        boolean dirty = false;

        for (AdventureDef def : AdventureCatalog.all()) {
            AdventureProgress progress = data.findAdventure(def.id());
            if (progress == null) {
                // Unlock later arcs when prerequisites met.
                if (!canUnlock(def, data)) {
                    continue;
                }
                progress = new AdventureProgress(def.id(), 0, false, 0);
                data.upsertAdventure(progress);
                dirty = true;
                player.sendSystemMessage(Component.literal("Adventure unlocked: " + def.title())
                        .withStyle(ChatFormatting.GOLD));
            }
            if (progress.completed()) {
                continue;
            }
            AdventureStep step = def.step(progress.stepIndex());
            if (step == null) {
                continue;
            }
            if (!matches(step, signal, detail, data, progress)) {
                continue;
            }
            AdventureProgress advanced = advance(def, progress, data);
            data.upsertAdventure(advanced);
            dirty = true;
            if (advanced.completed()) {
                completedNow.add(def.title());
                onComplete(player, data, def);
            } else {
                AdventureStep next = def.step(advanced.stepIndex());
                if (next != null) {
                    player.sendSystemMessage(Component.literal("Adventure • " + def.title() + ": " + next.hint())
                            .withStyle(ChatFormatting.AQUA));
                }
            }
        }

        if (dirty) {
            player.setData(ModAttachments.PLAYER_DATA, data.copy());
        }
        for (String title : completedNow) {
            player.sendSystemMessage(Component.literal("Adventure complete: " + title + "!")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        }
    }

    private static boolean canUnlock(AdventureDef def, PlayerEvaData data) {
        return switch (def.id()) {
            case "first_steps", "soft_companion" -> true;
            case "lost_stitches", "cartographers_trail", "seasonal_wanderer" ->
                    data.isAdventureComplete("first_steps");
            case "heart_of_the_hamlet" ->
                    data.isAdventureComplete("first_steps") && data.errandsCompleted() >= 1;
            case "ocean_lullaby", "mushroom_whispers", "cherry_picnic",
                 "badlands_dig", "mountain_beacon", "darkwood_lantern" ->
                    data.isAdventureComplete("first_steps") && data.houses().size() >= 1;
            default -> data.isAdventureComplete("first_steps");
        };
    }

    private static boolean matches(
            AdventureStep step,
            Signal signal,
            String detail,
            PlayerEvaData data,
            AdventureProgress progress) {
        return switch (step.type()) {
            case MEET_NPCS -> signal == Signal.MEET_NPC && data.houses().size() >= step.amount();
            case MEET_VARIANT -> signal == Signal.MEET_NPC
                    && countLabel(data, true, step.target()) >= step.amount();
            case MEET_JOB -> signal == Signal.MEET_NPC
                    && countLabel(data, false, step.target()) >= step.amount();
            case COMPLETE_ERRANDS -> signal == Signal.COMPLETE_ERRAND
                    && data.errandsCompleted() >= step.amount();
            case AWAKEN_PET -> signal == Signal.AWAKEN_PET && data.petsAwakened() >= step.amount();
            case PET_GLOW -> signal == Signal.PET_GLOW;
            case PET_FIND -> signal == Signal.PET_FIND;
            case LOCATE_SETTLEMENTS -> signal == Signal.LOCATE;
            case DISCOVER_TOWN -> signal == Signal.DISCOVER_TOWN;
            case OPEN_JOURNAL -> signal == Signal.OPEN_JOURNAL;
            case READ_MAIL -> signal == Signal.READ_MAIL;
            case GIFT_NPC -> signal == Signal.GIFT_NPC;
            case CHECK_CALENDAR -> signal == Signal.CHECK_CALENDAR;
        };
    }

    private static int countLabel(PlayerEvaData data, boolean biome, String wantRaw) {
        String want = wantRaw.toLowerCase(Locale.ROOT).replace('_', ' ');
        int n = 0;
        for (HouseIndexEntry e : data.houses()) {
            String label = biome ? e.biome() : e.job();
            if (label != null && label.toLowerCase(Locale.ROOT).contains(want)) {
                n++;
            }
        }
        return n;
    }

    private static AdventureProgress advance(AdventureDef def, AdventureProgress progress, PlayerEvaData data) {
        AdventureStep step = def.step(progress.stepIndex());
        if (step == null) {
            return progress.markDone();
        }
        if (step.type() == AdventureStepType.LOCATE_SETTLEMENTS) {
            int nextCounter = progress.counter() + 1;
            if (nextCounter < step.amount()) {
                return progress.withStep(progress.stepIndex(), nextCounter);
            }
        }
        int nextStep = progress.stepIndex() + 1;
        if (nextStep >= def.stepCount()) {
            return progress.markDone();
        }
        return new AdventureProgress(progress.id(), nextStep, false, 0);
    }

    private static void onComplete(ServerPlayer player, PlayerEvaData data, AdventureDef def) {
        data.addMail(new MailMessage(
                "Eva Adventures",
                "Adventure: " + def.title(),
                "You finished \"" + def.title() + "\". " + def.rewardBlurb(),
                player.level().getOverworldClockTime() / 24000L,
                false));
        // Soft unlock mail so players notice.
    }

    public static void listFor(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        ensureStarted(player, data);
        data = player.getData(ModAttachments.PLAYER_DATA);

        player.sendSystemMessage(Component.literal("Adventures (" + AdventureCatalog.size() + " stories)")
                .withStyle(ChatFormatting.GOLD));
        for (AdventureDef def : AdventureCatalog.all()) {
            AdventureProgress p = data.findAdventure(def.id());
            if (p == null) {
                if (canUnlock(def, data)) {
                    player.sendSystemMessage(Component.literal("○ " + def.title() + " — ready to start (/evamod adventure start " + def.id() + ")")
                            .withStyle(ChatFormatting.YELLOW));
                } else {
                    player.sendSystemMessage(Component.literal("◌ " + def.title() + " — locked")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                continue;
            }
            if (p.completed()) {
                player.sendSystemMessage(Component.literal("✓ " + def.title())
                        .withStyle(ChatFormatting.GREEN));
                continue;
            }
            AdventureStep step = def.step(p.stepIndex());
            String hint = step == null ? "…" : step.hint();
            player.sendSystemMessage(Component.literal("▸ " + def.title() + " [" + (p.stepIndex() + 1)
                            + "/" + def.stepCount() + "] " + hint)
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    public static int start(ServerPlayer player, String id) {
        AdventureDef def = AdventureCatalog.get(id);
        if (def == null) {
            player.sendSystemMessage(Component.literal("Unknown adventure id. Try /evamod adventure")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        AdventureProgress existing = data.findAdventure(id);
        if (existing != null && existing.completed()) {
            player.sendSystemMessage(Component.literal("Already completed: " + def.title())
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        if (existing == null && !canUnlock(def, data)) {
            player.sendSystemMessage(Component.literal("That adventure is still locked. Finish First Steps first.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (existing == null) {
            data.upsertAdventure(new AdventureProgress(id, 0, false, 0));
            player.setData(ModAttachments.PLAYER_DATA, data.copy());
            data = player.getData(ModAttachments.PLAYER_DATA);
            existing = data.findAdventure(id);
        }
        AdventureStep step = def.step(existing.stepIndex());
        player.sendSystemMessage(Component.literal(def.title() + ": " + def.summary())
                .withStyle(ChatFormatting.GOLD));
        if (step != null) {
            player.sendSystemMessage(Component.literal("Next: " + step.hint()).withStyle(ChatFormatting.AQUA));
        }
        return 1;
    }
}
