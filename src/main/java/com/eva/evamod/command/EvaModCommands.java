package com.eva.evamod.command;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Player-facing Eva Mod commands.
 * <p>
 * Discovery commands ({@code locate}, {@code near}, {@code houses}) work for everyone —
 * including worlds with cheats off (common on Mac/singleplayer). Only teleport needs op.
 * Unknown/typo input is caught and answered with plain-language help instead of Brigadier's
 * cryptic "Incorrect argument" / "Unknown or incomplete command" errors.
 */
@EventBusSubscriber(modid = EvaMod.MODID)
public final class EvaModCommands {
    public static final ResourceKey<Structure> NPC_HOUSE =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));
    public static final ResourceKey<Structure> NPC_TOWN =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_town"));
    public static final TagKey<Structure> NPC_SETTLEMENT_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));

    private static final double NEAR_RADIUS = 48.0;
    private static final Set<UUID> TIPPED_THIS_SESSION = ConcurrentHashMap.newKeySet();

    /** Known subcommands → canonical name (for typo suggestions). */
    private static final Map<String, String> COMMAND_ALIASES = Map.ofEntries(
            Map.entry("help", "help"),
            Map.entry("?", "help"),
            Map.entry("locate", "locate"),
            Map.entry("find", "locate"),
            Map.entry("search", "locate"),
            Map.entry("near", "near"),
            Map.entry("nearby", "near"),
            Map.entry("npcs", "near"),
            Map.entry("houses", "houses"),
            Map.entry("house", "houses"),
            Map.entry("homes", "houses"),
            Map.entry("home", "houses"),
            Map.entry("index", "houses"),
            Map.entry("visit", "visit"),
            Map.entry("tp", "visit"),
            Map.entry("teleport", "visit"),
            Map.entry("goto", "visit"));

    private static final List<String> KNOWN_COMMANDS =
            List.of("help", "locate", "near", "houses", "visit");

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> rootBuilder = Commands.literal("evamod")
                .executes(ctx -> help(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("?").executes(ctx -> help(ctx.getSource())))
                // Discovery — no cheats/op required (works with cheats-off worlds on any OS).
                .then(Commands.literal("locate")
                        .executes(ctx -> locate(ctx.getSource()))
                        .then(Commands.literal("reset").executes(ctx -> locateReset(ctx.getSource())))
                        .then(Commands.literal("clear").executes(ctx -> locateReset(ctx.getSource()))))
                .then(Commands.literal("find").executes(ctx -> locate(ctx.getSource())))
                .then(Commands.literal("search").executes(ctx -> locate(ctx.getSource())))
                .then(Commands.literal("near").executes(ctx -> near(ctx.getSource())))
                .then(Commands.literal("nearby").executes(ctx -> near(ctx.getSource())))
                .then(Commands.literal("npcs").executes(ctx -> near(ctx.getSource())))
                .then(Commands.literal("houses").executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("house").executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("homes").executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("home").executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("index").executes(ctx -> houses(ctx.getSource())))
                // Teleport — registered without .requires() so beginners see a clear message
                // instead of Brigadier's "Incorrect argument for command".
                .then(Commands.literal("visit").executes(ctx -> visit(ctx.getSource())))
                .then(Commands.literal("tp").executes(ctx -> visit(ctx.getSource())))
                .then(Commands.literal("teleport").executes(ctx -> visit(ctx.getSource())))
                .then(Commands.literal("goto").executes(ctx -> visit(ctx.getSource())))
                // Catch typos / leftover junk ("/evamod locatee", "/evamod loc ate", etc.).
                .then(Commands.argument("unknown", StringArgumentType.greedyString())
                        .executes(ctx -> unknownInput(
                                ctx.getSource(), StringArgumentType.getString(ctx, "unknown"))));

        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(rootBuilder);
        // Short + common mistypes (keyboard / OCR / autocorrect).
        dispatcher.register(Commands.literal("eva").redirect(root));
        dispatcher.register(Commands.literal("evanod").redirect(root));
        dispatcher.register(Commands.literal("evamood").redirect(root));
        dispatcher.register(Commands.literal("eva-mod").redirect(root));
    }

    /** One short tip after login so beginners know the command exists on any OS. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!TIPPED_THIS_SESSION.add(player.getUUID())) {
            return;
        }
        MutableComponent tip = Component.literal("Eva Mod: press ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("T").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" for chat, then type ").withStyle(ChatFormatting.GRAY))
                .append(suggestCommand("/evamod", "Show Eva Mod help"))
                .append(Component.literal(" — works without cheats. Talk to Biome Villagers to meet them.")
                        .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(tip);
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Eva Mod — beginner guide")
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Open chat with T (same on Mac, Windows, Linux), then type a command below.")
                .withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod locate", "Find next house/town"),
                Component.literal(" — find the next nearest house/town (no cheats needed)")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod locate reset", "Clear locate skip list"),
                Component.literal(" — clear the locate skip list")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod near", "List nearby NPCs"),
                Component.literal(" — list Biome NPCs near you")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod houses", "Show house index"),
                Component.literal(" — houses of NPCs you have talked to")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod visit", "Teleport to nearest settlement"),
                Component.literal(" — teleport to a nearby settlement (needs cheats/op)")), false);
        source.sendSuccess(() -> Component.literal("Tip: if a command says it needs cheats, open to LAN and enable Allow Cheats, or recreate the world with cheats on.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        source.sendSuccess(() -> Component.literal("Also works: /eva  |  shortcuts: find, nearby, homes, tp")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int locate(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        try {
            if (SettlementLocator.isSearching(player.getUUID())) {
                source.sendFailure(Component.literal(
                        "Already searching — wait a moment, or run /evamod locate reset to cancel."));
                return 0;
            }
            if (!structuresAvailable(player.level())) {
                source.sendFailure(Component.literal(
                        "Eva Mod houses are not loaded in this world. Make sure the mod is installed on the server/world and you are in the Overworld."));
                return 0;
            }
            ServerLevel level = player.level();
            PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
            SettlementLocator.LocateResult found = SettlementLocator.tryInstant(level, player.blockPosition(), data);
            if (found != null) {
                data.rememberLocated(found.pos());
                player.setData(ModAttachments.PLAYER_DATA, data.copy());
                SettlementLocator.rememberWorldCache(level, found);
                String label = found.town() ? "npc_town (hamlet)" : "npc_house";
                int npcHint = found.town()
                        ? SettlementLocator.countNpcsNear(level, found.pos(), 48)
                        : SettlementLocator.countNpcsNear(level, found.pos(), 16);
                source.sendSuccess(() -> {
                    var msg = Component.literal("Found " + label + " at ").withStyle(ChatFormatting.GREEN);
                    msg = SettlementLocator.clickablePos(msg, found.pos());
                    if (found.town()) {
                        msg.append(Component.literal(npcHint > 0
                                        ? " — town with " + npcHint + " NPC(s) nearby"
                                        : " — multi-NPC hamlet")
                                .withStyle(ChatFormatting.GRAY));
                    }
                    msg.append(Component.literal("  (run again for the next one; /evamod visit to teleport if cheats are on)")
                            .withStyle(ChatFormatting.DARK_GRAY));
                    return msg;
                }, false);
                return 1;
            }
            source.sendSuccess(() -> Component.literal(
                            "No settlement in loaded chunks — searching the nearby world. This can take a few seconds…")
                    .withStyle(ChatFormatting.YELLOW), false);
            SettlementLocator.startSearch(player, data);
            return 1;
        } catch (Exception e) {
            EvaMod.LOGGER.error("/evamod locate failed for {}", player.getGameProfile().name(), e);
            source.sendFailure(Component.literal(
                    "Locate hit an unexpected error. Try again, or travel a bit and use /evamod locate reset."));
            return 0;
        }
    }

    private static int locateReset(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        SettlementLocator.cancel(player.getUUID());
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        data.resetLocated();
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        source.sendSuccess(() -> Component.literal(
                        "Locate skip list cleared (and any search cancelled). Your house index is unchanged. Try /evamod locate again.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int near(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        try {
            AABB box = player.getBoundingBox().inflate(NEAR_RADIUS);
            List<BiomeNpc> npcs = player.level().getEntitiesOfClass(BiomeNpc.class, box, Entity::isAlive);
            npcs.sort(Comparator.comparingDouble(player::distanceToSqr));
            if (npcs.isEmpty()) {
                source.sendSuccess(() -> Component.literal(
                                "No Biome NPCs within " + (int) NEAR_RADIUS
                                        + " blocks. Try /evamod locate to find a house, then walk there and talk to them.")
                        .withStyle(ChatFormatting.YELLOW), false);
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Nearby NPCs (" + npcs.size() + "):")
                    .withStyle(ChatFormatting.GOLD), false);
            for (BiomeNpc npc : npcs.stream().limit(12).toList()) {
                BlockPos pos = npc.blockPosition();
                String line = npc.getNpcName() + " — " + npc.getPersonality().getDisplayName()
                        + " " + npc.getJob().getDisplayName();
                source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(line + " @ "), pos), false);
            }
            return npcs.size();
        } catch (Exception e) {
            EvaMod.LOGGER.error("/evamod near failed for {}", player.getGameProfile().name(), e);
            source.sendFailure(Component.literal("Could not list nearby NPCs. Try again in a moment."));
            return 0;
        }
    }

    private static int houses(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        List<HouseIndexEntry> houses = player.getData(ModAttachments.PLAYER_DATA).houses();
        if (houses.isEmpty()) {
            source.sendSuccess(() -> Component.literal("House index empty — right-click Biome Villagers to meet them.")
                    .withStyle(ChatFormatting.YELLOW), false);
            source.sendSuccess(() -> line(
                    Component.literal("Need a house first? ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod locate", "Find next house/town")), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("House Index (" + houses.size() + "):")
                .withStyle(ChatFormatting.GOLD), false);
        for (HouseIndexEntry entry : houses) {
            String label = entry.npcName() + " — " + entry.personality() + " " + entry.biome()
                    + " " + entry.job() + " @ ";
            source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(label), entry.homePos()), false);
        }
        return houses.size();
    }

    private static int visit(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        if (!Commands.LEVEL_GAMEMASTERS.check(source.permissions())) {
            source.sendFailure(Component.literal(
                    "Teleport needs cheats or op. On singleplayer: Open to LAN → Allow Cheats → Start LAN World, then try again."));
            source.sendSuccess(() -> line(
                    Component.literal("No cheats? Use ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod locate", "Find next house/town"),
                    Component.literal(" for coordinates instead.").withStyle(ChatFormatting.GRAY)), false);
            return 0;
        }
        try {
            ServerLevel level = player.level();
            PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
            SettlementLocator.LocateResult found = SettlementLocator.tryInstant(level, player.blockPosition(), data);
            if (found == null) {
                BlockPos hit = level.findNearestMapStructure(NPC_SETTLEMENT_TAG, player.blockPosition(), 64, false);
                if (hit == null) {
                    source.sendFailure(Component.literal(
                            "No npc_house/npc_town nearby yet. Run /evamod locate first (works without cheats), wait for a result, then visit."));
                    return 0;
                }
                found = SettlementLocator.refineResult(level, hit);
            }
            SettlementLocator.rememberWorldCache(level, found);
            BlockPos land = SettlementLocator.safeTeleportPos(level, found.pos());
            player.teleportTo(land.getX() + 0.5, land.getY(), land.getZ() + 0.5);
            BlockPos finalLand = land;
            source.sendSuccess(() -> SettlementLocator.clickablePos(
                    Component.literal("Visited settlement at ").withStyle(ChatFormatting.AQUA), finalLand), false);
            return 1;
        } catch (Exception e) {
            EvaMod.LOGGER.error("/evamod visit failed for {}", player.getGameProfile().name(), e);
            source.sendFailure(Component.literal(
                    "Teleport failed. Try /evamod locate for coordinates, or enable cheats and retry."));
            return 0;
        }
    }

    private static int unknownInput(CommandSourceStack source, String raw) {
        String cleaned = normalizeToken(raw);
        if (cleaned.isEmpty()) {
            return help(source);
        }

        // "/evamod locate reset" style extras already handled by tree; here we get leftovers.
        String[] parts = cleaned.split("\\s+");
        String head = parts[0];
        String canonical = COMMAND_ALIASES.get(head);
        if (canonical != null) {
            // User typed a known word but with trailing junk, e.g. "locate please"
            source.sendFailure(Component.literal(
                    "Almost — Eva Mod does not take extra words after the command."));
            return suggestCanonical(source, canonical, parts);
        }

        String suggestion = closestCommand(head);
        source.sendFailure(Component.literal("Unknown Eva Mod command: \"" + head + "\""));
        if (suggestion != null) {
            source.sendSuccess(() -> line(
                    Component.literal("Did you mean ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod " + suggestion, "Run /evamod " + suggestion),
                    Component.literal("?").withStyle(ChatFormatting.GRAY)), false);
        } else {
            source.sendSuccess(() -> line(
                    Component.literal("Type ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod", "Show Eva Mod help"),
                    Component.literal(" for the full list.").withStyle(ChatFormatting.GRAY)), false);
        }
        return 0;
    }

    private static int suggestCanonical(CommandSourceStack source, String canonical, String[] parts) {
        if ("locate".equals(canonical) && parts.length >= 2
                && (parts[1].equals("reset") || parts[1].equals("clear"))) {
            source.sendSuccess(() -> line(
                    Component.literal("Try exactly: ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod locate reset", "Clear locate skip list")), false);
            return 0;
        }
        source.sendSuccess(() -> line(
                Component.literal("Try exactly: ").withStyle(ChatFormatting.GRAY),
                suggestCommand("/evamod " + canonical, "Run /evamod " + canonical)), false);
        return 0;
    }

    private static ServerPlayer requirePlayer(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal(
                    "This Eva Mod command is for players in-game (open chat with T and run it there)."));
            return null;
        }
        return player;
    }

    private static boolean structuresAvailable(ServerLevel level) {
        try {
            level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(NPC_HOUSE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String normalizeToken(String raw) {
        if (raw == null) {
            return "";
        }
        // Strip zero-width / non-breaking / smart punctuation beginners paste from phones/docs.
        String s = raw
                .replace('\u00A0', ' ')
                .replace('\u200B', ' ')
                .replace('\uFEFF', ' ')
                .replace('—', ' ')
                .replace('–', ' ')
                .replace('“', ' ')
                .replace('”', ' ')
                .replace('‘', ' ')
                .replace('’', ' ')
                .trim()
                .toLowerCase(Locale.ROOT);
        // Collapse whitespace.
        return s.replaceAll("\\s+", " ");
    }

    private static String closestCommand(String input) {
        String best = null;
        int bestDist = Integer.MAX_VALUE;
        for (String cmd : KNOWN_COMMANDS) {
            int dist = levenshtein(input, cmd);
            // Tight threshold so short junk does not false-suggest.
            int max = Math.max(1, cmd.length() / 3);
            if (dist <= max && dist < bestDist) {
                bestDist = dist;
                best = cmd;
            }
        }
        // Prefix / contains soft match for partial typing.
        if (best == null) {
            for (String cmd : KNOWN_COMMANDS) {
                if (cmd.startsWith(input) || input.startsWith(cmd) || cmd.contains(input)) {
                    return cmd;
                }
            }
        }
        return best;
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[b.length()];
    }

    private static MutableComponent suggestCommand(String command, String hover) {
        return Component.literal(command).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
    }

    private static MutableComponent line(Component... parts) {
        MutableComponent out = Component.empty();
        for (Component part : parts) {
            out.append(part);
        }
        return out;
    }

    private EvaModCommands() {
    }
}
