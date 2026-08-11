package com.eva.evamod.command;

import com.eva.evamod.EvaMod;
import com.eva.evamod.ModVersions;
import com.eva.evamod.calendar.SeasonCalendar;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.StuffedPet;import com.eva.evamod.friendship.Hearts;
import com.eva.evamod.mail.MailMessage;
import com.eva.evamod.mail.MailService;
import com.eva.evamod.player.GuideBookService;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.quest.Errand;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.world.HomesteadBootstrap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

/**
 * Player-facing Eva Mod 2.0 commands.
 * <p>
 * Discovery ({@code locate}, {@code town}, {@code near}, {@code journal}, {@code mail}) works
 * without cheats. Teleport ({@code visit}, {@code town visit}) needs op/cheats and lands
 * inside a house (bed / interior), never at Y≈0.
 */
@EventBusSubscriber(modid = EvaMod.MODID)
public final class EvaModCommands {
    public static final ResourceKey<Structure> NPC_HOUSE =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));
    public static final ResourceKey<Structure> NPC_TOWN =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_town"));
    public static final TagKey<Structure> NPC_SETTLEMENT_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));
    public static final TagKey<Structure> NPC_TOWN_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_town"));

    private static final double NEAR_RADIUS = 48.0;

    private static final Map<String, String> COMMAND_ALIASES = Map.ofEntries(
            Map.entry("help", "help"),
            Map.entry("?", "help"),
            Map.entry("locate", "locate"),
            Map.entry("find", "locate"),
            Map.entry("search", "locate"),
            Map.entry("town", "town"),
            Map.entry("towns", "town"),
            Map.entry("hamlet", "town"),
            Map.entry("near", "near"),
            Map.entry("nearby", "near"),
            Map.entry("npcs", "near"),
            Map.entry("houses", "journal"),
            Map.entry("house", "journal"),
            Map.entry("homes", "journal"),
            Map.entry("home", "journal"),
            Map.entry("index", "journal"),
            Map.entry("journal", "journal"),
            Map.entry("visit", "visit"),
            Map.entry("tp", "visit"),
            Map.entry("teleport", "visit"),
            Map.entry("goto", "visit"),
            Map.entry("mail", "mail"),
            Map.entry("letters", "mail"),
            Map.entry("calendar", "calendar"),
            Map.entry("season", "calendar"),
            Map.entry("date", "calendar"),
            Map.entry("errand", "errand"),
            Map.entry("quest", "errand"),
            Map.entry("book", "book"),
            Map.entry("primer", "book"),
            Map.entry("guide", "book"),
            Map.entry("settle", "settle"),
            Map.entry("found", "settle"),
            Map.entry("homestead", "settle"),
            Map.entry("version", "version"),
            Map.entry("pet", "pet"),
            Map.entry("pets", "pet"),
            Map.entry("companion", "pet"),
            Map.entry("adventure", "adventure"),
            Map.entry("adventures", "adventure"),
            Map.entry("story", "adventure"),
            Map.entry("stories", "adventure"));

    private static final List<String> KNOWN_COMMANDS =
            List.of("help", "locate", "town", "near", "journal", "visit", "mail", "calendar", "errand", "book",
                    "settle", "version", "pet", "adventure");
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> rootBuilder = Commands.literal("evamod")
                .executes(ctx -> help(ctx.getSource()))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("?").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("version").executes(ctx -> version(ctx.getSource())))
                .then(Commands.literal("book")
                        .executes(ctx -> book(ctx.getSource()))
                        .then(extraArgs("book")))
                .then(Commands.literal("primer")
                        .executes(ctx -> book(ctx.getSource()))
                        .then(extraArgs("book")))
                .then(Commands.literal("guide")
                        .executes(ctx -> book(ctx.getSource()))
                        .then(extraArgs("book")))
                .then(Commands.literal("settle")
                        .executes(ctx -> settle(ctx.getSource()))
                        .then(extraArgs("settle")))
                .then(Commands.literal("found")
                        .executes(ctx -> settle(ctx.getSource()))
                        .then(extraArgs("settle")))
                .then(Commands.literal("homestead")
                        .executes(ctx -> settle(ctx.getSource()))
                        .then(extraArgs("settle")))
                .then(Commands.literal("locate")
                        .executes(ctx -> locate(ctx.getSource(), false))
                        .then(Commands.literal("reset").executes(ctx -> locateReset(ctx.getSource())))
                        .then(Commands.literal("clear").executes(ctx -> locateReset(ctx.getSource())))
                        .then(extraArgs("locate")))
                .then(Commands.literal("find")
                        .executes(ctx -> locate(ctx.getSource(), false))
                        .then(extraArgs("locate")))
                .then(Commands.literal("search")
                        .executes(ctx -> locate(ctx.getSource(), false))
                        .then(extraArgs("locate")))
                .then(Commands.literal("town")
                        .executes(ctx -> locate(ctx.getSource(), true))
                        .then(Commands.literal("visit").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(Commands.literal("tp").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(Commands.literal("goto").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(extraArgs("town")))
                .then(Commands.literal("towns")
                        .executes(ctx -> locate(ctx.getSource(), true))
                        .then(Commands.literal("visit").executes(ctx -> visit(ctx.getSource(), true))))
                .then(Commands.literal("hamlet")
                        .executes(ctx -> locate(ctx.getSource(), true))
                        .then(Commands.literal("visit").executes(ctx -> visit(ctx.getSource(), true))))
                .then(Commands.literal("near")
                        .executes(ctx -> near(ctx.getSource()))
                        .then(extraArgs("near")))
                .then(Commands.literal("nearby")
                        .executes(ctx -> near(ctx.getSource()))
                        .then(extraArgs("near")))
                .then(Commands.literal("npcs")
                        .executes(ctx -> near(ctx.getSource()))
                        .then(extraArgs("near")))
                .then(Commands.literal("journal")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("houses")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("house")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("homes")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("home")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("index")
                        .executes(ctx -> journal(ctx.getSource()))
                        .then(extraArgs("journal")))
                .then(Commands.literal("visit")
                        .executes(ctx -> visit(ctx.getSource(), false))
                        .then(Commands.literal("town").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(extraArgs("visit")))
                .then(Commands.literal("tp")
                        .executes(ctx -> visit(ctx.getSource(), false))
                        .then(Commands.literal("town").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(extraArgs("visit")))
                .then(Commands.literal("teleport")
                        .executes(ctx -> visit(ctx.getSource(), false))
                        .then(Commands.literal("town").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(extraArgs("visit")))
                .then(Commands.literal("goto")
                        .executes(ctx -> visit(ctx.getSource(), false))
                        .then(Commands.literal("town").executes(ctx -> visit(ctx.getSource(), true)))
                        .then(extraArgs("visit")))
                .then(Commands.literal("mail")
                        .executes(ctx -> mail(ctx.getSource()))
                        .then(extraArgs("mail")))
                .then(Commands.literal("letters")
                        .executes(ctx -> mail(ctx.getSource()))
                        .then(extraArgs("mail")))
                .then(Commands.literal("calendar")
                        .executes(ctx -> calendar(ctx.getSource()))
                        .then(extraArgs("calendar")))
                .then(Commands.literal("season")
                        .executes(ctx -> calendar(ctx.getSource()))
                        .then(extraArgs("calendar")))
                .then(Commands.literal("date")
                        .executes(ctx -> calendar(ctx.getSource()))
                        .then(extraArgs("calendar")))
                .then(Commands.literal("errand")
                        .executes(ctx -> errand(ctx.getSource()))
                        .then(extraArgs("errand")))
                .then(Commands.literal("quest")
                        .executes(ctx -> errand(ctx.getSource()))
                        .then(extraArgs("errand")))
                .then(Commands.literal("pet")
                        .executes(ctx -> petStatus(ctx.getSource()))
                        .then(Commands.literal("sit").executes(ctx -> petSit(ctx.getSource(), true)))
                        .then(Commands.literal("follow").executes(ctx -> petSit(ctx.getSource(), false)))
                        .then(Commands.literal("glow").executes(ctx -> petGlow(ctx.getSource())))
                        .then(Commands.literal("find").executes(ctx -> petFind(ctx.getSource())))
                        .then(Commands.literal("here").executes(ctx -> petHere(ctx.getSource())))
                        .then(extraArgs("pet")))
                .then(Commands.literal("pets")
                        .executes(ctx -> petStatus(ctx.getSource()))
                        .then(Commands.literal("sit").executes(ctx -> petSit(ctx.getSource(), true)))
                        .then(Commands.literal("follow").executes(ctx -> petSit(ctx.getSource(), false)))
                        .then(Commands.literal("glow").executes(ctx -> petGlow(ctx.getSource())))
                        .then(Commands.literal("find").executes(ctx -> petFind(ctx.getSource())))
                        .then(Commands.literal("here").executes(ctx -> petHere(ctx.getSource()))))
                .then(Commands.literal("companion")
                        .executes(ctx -> petStatus(ctx.getSource())))
                .then(Commands.literal("adventure")
                        .executes(ctx -> adventureList(ctx.getSource()))
                        .then(Commands.literal("list").executes(ctx -> adventureList(ctx.getSource())))
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> adventureStart(
                                                ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                        .then(extraArgs("adventure")))
                .then(Commands.literal("adventures")
                        .executes(ctx -> adventureList(ctx.getSource()))
                        .then(extraArgs("adventure")))
                .then(Commands.literal("story")
                        .executes(ctx -> adventureList(ctx.getSource()))
                        .then(extraArgs("adventure")))                .then(Commands.argument("unknown", StringArgumentType.greedyString())
                        .executes(ctx -> unknownInput(
                                ctx.getSource(), StringArgumentType.getString(ctx, "unknown"))));

        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(rootBuilder);
        dispatcher.register(Commands.literal("eva").redirect(root));
        dispatcher.register(Commands.literal("evanod").redirect(root));
        dispatcher.register(Commands.literal("evamood").redirect(root));
        dispatcher.register(Commands.literal("eva-mod").redirect(root));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Eva Mod " + ModVersions.DISPLAY + " — " + ModVersions.CODENAME)
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Open your Homestead Primer book, or type a command below.")
                .withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod book", "Get Homestead Primer"),
                Component.literal(" — get the command primer book")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod town", "Find nearest town"),
                Component.literal(" — find the nearest multi-NPC town (no cheats)")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod locate", "Find next house/town"),
                Component.literal(" — find the next nearest house or town")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod settle", "Founder's Homestead"),
                Component.literal(" — plant one starter home (pre-mod / explored worlds)")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod town visit", "Teleport into a town house"),
                Component.literal(" — teleport inside a town house (needs cheats)")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod visit", "Teleport to nearest settlement"),
                Component.literal(" — teleport into a house (bed/interior, needs cheats)")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod journal", "Open homestead journal"),
                Component.literal(" — friends, hearts, birthdays")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod mail", "Read NPC letters"),
                Component.literal(" — letters from NPCs")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod calendar", "Show season & festival"),
                Component.literal(" — season, date, festival")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod errand", "Show active errand"),
                Component.literal(" — your active help-wanted errand")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod near", "List nearby NPCs"),
                Component.literal(" — NPCs near you")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod pet", "Pet companion"),
                Component.literal(" — stuffed pet status & utilities")), false);
        source.sendSuccess(() -> line(
                suggestCommand("/evamod adventure", "Adventure stories"),
                Component.literal(" — multi-step cozy adventure stories")), false);        source.sendSuccess(() -> Component.literal("1.x jars are outdated. Saves migrate forward via schema versions.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int version(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Eva Mod " + ModVersions.DISPLAY + " " + ModVersions.CODENAME
                        + " (Minecraft 26.2) · player schema " + ModVersions.PLAYER_SCHEMA
                        + " · world schema " + ModVersions.WORLD_SCHEMA)
                .withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("Supported from " + ModVersions.MIN_SUPPORTED_DISPLAY
                        + ". Versions 1.0–1.1.x are outdated and unsupported.")
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int book(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        GuideBookService.giveAnother(player);
        source.sendSuccess(() -> Component.literal("Homestead Primer added to your inventory (or dropped nearby).")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int settle(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        return HomesteadBootstrap.settle(player);
    }

    private static int locate(CommandSourceStack source, boolean townsOnly) {
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
            if (!structuresAvailable(player.level(), townsOnly)) {
                source.sendFailure(Component.literal(
                        "Eva Mod settlements are not loaded in this world. Make sure the mod is installed and you are in the Overworld."));
                return 0;
            }
            ServerLevel level = player.level();
            PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
            SettlementLocator.LocateResult found = SettlementLocator.tryInstant(level, player.blockPosition(), data, townsOnly);
            if (found != null) {
                data.rememberLocated(found.pos());
                player.setData(ModAttachments.PLAYER_DATA, data.copy());
                SettlementLocator.rememberWorldCache(level, found);
                BlockPos land = SettlementLocator.safeInteriorTeleportPos(level, found.pos(), found.town());
                String label = found.town() ? "npc_town (hamlet)" : "npc_house";
                int npcHint = found.town()
                        ? SettlementLocator.countNpcsNear(level, found.pos(), 48)
                        : SettlementLocator.countNpcsNear(level, found.pos(), 16);
                source.sendSuccess(() -> {
                    var msg = Component.literal("Found " + label + " — safe landing ").withStyle(ChatFormatting.GREEN);
                    msg = SettlementLocator.clickablePos(msg, land);
                    if (found.town()) {
                        msg.append(Component.literal(npcHint > 0
                                        ? " — town with " + npcHint + " NPC(s) nearby"
                                        : " — multi-NPC hamlet")
                                .withStyle(ChatFormatting.GRAY));
                    }
                    String visitCmd = found.town() ? "/evamod town visit" : "/evamod visit";
                    msg.append(Component.literal("  (run again for the next one; " + visitCmd + " if cheats are on)")
                            .withStyle(ChatFormatting.DARK_GRAY));
                    return msg;
                }, false);
                com.eva.evamod.adventure.AdventureService.signal(
                        player, com.eva.evamod.adventure.AdventureService.Signal.LOCATE);
                if (found.town()) {
                    PlayerEvaData after = player.getData(ModAttachments.PLAYER_DATA);
                    if (after.discoverLandmark("town:" + found.pos().getX() + "," + found.pos().getZ())) {
                        player.setData(ModAttachments.PLAYER_DATA, after.copy());
                    }
                    com.eva.evamod.adventure.AdventureService.signal(
                            player, com.eva.evamod.adventure.AdventureService.Signal.DISCOVER_TOWN);
                }
                return 1;
            }
            source.sendSuccess(() -> Component.literal(townsOnly
                            ? "No town in loaded chunks — searching the nearby world…"
                            : "No settlement in loaded chunks — searching the nearby world…")
                    .withStyle(ChatFormatting.YELLOW), false);
            SettlementLocator.startSearch(player, data, townsOnly);
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
                        "Locate skip list cleared. Try /evamod town or /evamod locate again.")
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
                                        + " blocks. Try /evamod town to find a hamlet.")
                        .withStyle(ChatFormatting.YELLOW), false);
                return 0;
            }
            source.sendSuccess(() -> Component.literal("Nearby NPCs (" + npcs.size() + "):")
                    .withStyle(ChatFormatting.GOLD), false);
            long day = player.level().getOverworldClockTime() / 24000L;
            for (BiomeNpc npc : npcs.stream().limit(12).toList()) {
                int hearts = Hearts.fromReputation(npc.getMemory().get(player.getUUID(), day).reputation);
                BlockPos pos = npc.blockPosition();
                String line = npc.getNpcName() + " " + Hearts.bar(hearts) + " — "
                        + npc.getPersonality().getDisplayName() + " " + npc.getJob().getDisplayName();
                source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(line + " @ "), pos), false);
            }
            return npcs.size();
        } catch (Exception e) {
            EvaMod.LOGGER.error("/evamod near failed for {}", player.getGameProfile().name(), e);
            source.sendFailure(Component.literal("Could not list nearby NPCs. Try again in a moment."));
            return 0;
        }
    }

    private static int journal(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        com.eva.evamod.adventure.AdventureService.signal(
                player, com.eva.evamod.adventure.AdventureService.Signal.OPEN_JOURNAL);
        List<HouseIndexEntry> houses = player.getData(ModAttachments.PLAYER_DATA).houses();
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        int done = data.adventuresCompleted();
        if (done > 0 || !data.adventures().isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                            "Adventures: " + done + " complete · /evamod adventure for stories")
                    .withStyle(ChatFormatting.DARK_AQUA), false);
        }
        if (houses.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Journal empty — right-click Biome Villagers to meet them.")
                    .withStyle(ChatFormatting.YELLOW), false);
            source.sendSuccess(() -> line(
                    Component.literal("Need a town? ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod town", "Find nearest town")), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Homestead Journal (" + houses.size() + " friends):")
                .withStyle(ChatFormatting.GOLD), false);
        for (HouseIndexEntry entry : houses) {
            String label = entry.npcName() + " " + Hearts.bar(entry.hearts())
                    + " — " + entry.personality() + " " + entry.biome() + " " + entry.job()
                    + " · birthday " + entry.birthday() + " @ ";
            source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(label), entry.homePos()), false);
        }
        return houses.size();
    }

    private static int visit(CommandSourceStack source, boolean townsOnly) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        if (!Commands.LEVEL_GAMEMASTERS.check(source.permissions())) {
            source.sendFailure(Component.literal(
                    "Teleport needs cheats or op. On singleplayer: Open to LAN → Allow Cheats → Start LAN World."));
            source.sendSuccess(() -> line(
                    Component.literal("No cheats? Use ").withStyle(ChatFormatting.GRAY),
                    suggestCommand(townsOnly ? "/evamod town" : "/evamod locate", "Find settlement"),
                    Component.literal(" for safe landing coordinates.").withStyle(ChatFormatting.GRAY)), false);
            return 0;
        }
        try {
            ServerLevel level = player.level();
            PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
            SettlementLocator.LocateResult found = SettlementLocator.tryInstant(level, player.blockPosition(), data, townsOnly);
            if (found == null) {
                BlockPos hit = level.findNearestMapStructure(
                        townsOnly ? NPC_TOWN_TAG : NPC_SETTLEMENT_TAG, player.blockPosition(), 96, false);
                if (hit == null) {
                    source.sendFailure(Component.literal(townsOnly
                            ? "No npc_town nearby yet. Run /evamod town first, wait for a result, then visit."
                            : "No settlement nearby yet. Run /evamod locate or /evamod town first."));
                    return 0;
                }
                found = SettlementLocator.refineResult(level, hit);
                if (townsOnly && !found.town()) {
                    source.sendFailure(Component.literal(
                            "Found a solitary house, not a town. Try /evamod town to search specifically for hamlets."));
                    return 0;
                }
            }
            SettlementLocator.rememberWorldCache(level, found);
            BlockPos land = SettlementLocator.safeInteriorTeleportPos(level, found.pos(), found.town() || townsOnly);
            player.teleportTo(land.getX() + 0.5, land.getY(), land.getZ() + 0.5);
            BlockPos finalLand = land;
            String where = found.town() ? "town house" : "house";
            source.sendSuccess(() -> SettlementLocator.clickablePos(
                    Component.literal("Visited " + where + " interior at ").withStyle(ChatFormatting.AQUA), finalLand), false);
            return 1;
        } catch (Exception e) {
            EvaMod.LOGGER.error("/evamod visit failed for {}", player.getGameProfile().name(), e);
            source.sendFailure(Component.literal(
                    "Teleport failed. Try /evamod town for coordinates, or enable cheats and retry."));
            return 0;
        }
    }

    private static int mail(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        MailService.tryDeliver(player);
        data = player.getData(ModAttachments.PLAYER_DATA);
        List<MailMessage> letters = data.mail();
        if (letters.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                            "Mailbox empty. Meet NPCs, raise hearts, and check back — birthdays and festivals bring letters.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Mailbox (" + letters.size() + "):")
                .withStyle(ChatFormatting.GOLD), false);
        for (MailMessage letter : letters.stream().limit(8).toList()) {
            ChatFormatting color = letter.read() ? ChatFormatting.GRAY : ChatFormatting.YELLOW;
            source.sendSuccess(() -> Component.literal(
                            (letter.read() ? "· " : "✉ ") + letter.from() + " — " + letter.subject())
                    .withStyle(color), false);
            source.sendSuccess(() -> Component.literal("  " + letter.body())
                    .withStyle(ChatFormatting.WHITE), false);
        }
        data.markAllMailRead();
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        com.eva.evamod.adventure.AdventureService.signal(
                player, com.eva.evamod.adventure.AdventureService.Signal.READ_MAIL);        return letters.size();
    }

    private static int calendar(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        long day = player != null
                ? player.level().getOverworldClockTime() / 24000L
                : source.getLevel().getOverworldClockTime() / 24000L;
        source.sendSuccess(() -> Component.literal("Today is " + SeasonCalendar.formatDate(day))
                .withStyle(ChatFormatting.GOLD), false);
        if (SeasonCalendar.isFestival(day)) {
            source.sendSuccess(() -> Component.literal("Festival today: " + SeasonCalendar.festivalName(day) + "!")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        } else {
            int until = SeasonCalendar.FESTIVAL_DAY - SeasonCalendar.dayInSeason(day);
            if (until < 0) {
                until += SeasonCalendar.DAYS_PER_SEASON;
            }
            int finalUntil = until;
            source.sendSuccess(() -> Component.literal(
                            "Next festival (" + SeasonCalendar.festivalName(day + finalUntil) + ") in "
                                    + finalUntil + " day(s).")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        if (player != null) {
            com.eva.evamod.adventure.AdventureService.signal(
                    player, com.eva.evamod.adventure.AdventureService.Signal.CHECK_CALENDAR);
        }        return 1;
    }

    private static int errand(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        Errand errand = player.getData(ModAttachments.PLAYER_DATA).activeErrand();
        if (errand == null || errand.completed()) {
            source.sendSuccess(() -> Component.literal(
                            "No active errand. Talk to friendly NPCs (2+ hearts) and press Help in dialogue.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Active errand: " + errand.describe())
                .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal("Reward: +" + errand.rewardRep() + " friendship when delivered.")
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int petStatus(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        StuffedPet pet = StuffedPet.findOwnedPet(player);
        if (pet == null) {
            source.sendSuccess(() -> Component.literal("No awakened pet nearby. Place an Alive Plush on the ground!")
                    .withStyle(ChatFormatting.YELLOW), false);
            if (data.petsAwakened() > 0) {
                source.sendSuccess(() -> Component.literal("You've awakened " + data.petsAwakened()
                                + " pet(s) before — they may be far away. Try /evamod pet here when close.")
                        .withStyle(ChatFormatting.GRAY), false);
            }
            tipPetCommands(source);
            return 1;
        }
        String kind = pet.getKind().getDisplayName();
        String pose = pet.isOrderedToSit() ? "sitting" : "following";
        String carried = pet.getCarriedItem().isEmpty()
                ? "nothing"
                : pet.getCarriedItem().getHoverName().getString();
        source.sendSuccess(() -> Component.literal(pet.getName().getString() + " the " + kind
                        + " — " + pose
                        + (pet.isGlowUtility() ? ", glowing" : "")
                        + ", ribbon " + pet.getRibbonColor().getName()
                        + ", carrying " + carried
                        + ".")
                .withStyle(ChatFormatting.GREEN), false);
        tipPetCommands(source);
        return 1;
    }

    private static void tipPetCommands(CommandSourceStack source) {
        source.sendSuccess(() -> line(
                suggestCommand("/evamod pet sit", "Sit"),
                Component.literal(" · "),
                suggestCommand("/evamod pet follow", "Follow"),
                Component.literal(" · "),
                suggestCommand("/evamod pet glow", "Glow"),
                Component.literal(" · "),
                suggestCommand("/evamod pet find", "Find settlement"),
                Component.literal(" · "),
                suggestCommand("/evamod pet here", "Come here")), false);
    }

    private static int petSit(CommandSourceStack source, boolean sit) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        StuffedPet pet = StuffedPet.findOwnedPet(player);
        if (pet == null) {
            source.sendFailure(Component.literal("No pet found. Awaken an Alive Plush first."));
            return 0;
        }
        pet.setOrderedToSit(sit);
        pet.getNavigation().stop();
        pet.setBubbleText(sit ? "sit" : "ok!");
        source.sendSuccess(() -> Component.literal(sit
                        ? pet.getName().getString() + " sits patiently."
                        : pet.getName().getString() + " trots after you.")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int petGlow(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        StuffedPet pet = StuffedPet.findOwnedPet(player);
        if (pet == null) {
            source.sendFailure(Component.literal("No pet found. Awaken an Alive Plush first."));
            return 0;
        }
        pet.toggleGlow(player);
        return 1;
    }

    private static int petFind(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        StuffedPet pet = StuffedPet.findOwnedPet(player);
        if (pet == null) {
            source.sendFailure(Component.literal("No pet found. Awaken an Alive Plush first."));
            return 0;
        }
        return pet.findSettlementHint(player);
    }

    private static int petHere(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        StuffedPet pet = StuffedPet.findOwnedPet(player);
        if (pet == null) {
            source.sendFailure(Component.literal(
                    "No pet found nearby. They may be unloaded — walk closer to where you left them."));
            return 0;
        }
        if (pet.level() != player.level()) {
            source.sendFailure(Component.literal("Your pet is in another dimension. Travel there, then try again."));
            return 0;
        }
        pet.softTeleportToOwner(player);
        source.sendSuccess(() -> Component.literal(pet.getName().getString() + " pops over to you.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int adventureList(CommandSourceStack source) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        com.eva.evamod.adventure.AdventureService.listFor(player);
        return 1;
    }

    private static int adventureStart(CommandSourceStack source, String id) {
        ServerPlayer player = requirePlayer(source);
        if (player == null) {
            return 0;
        }
        return com.eva.evamod.adventure.AdventureService.start(player, id);
    }
    private static RequiredArgumentBuilder<CommandSourceStack, String> extraArgs(String canonical) {
        return Commands.argument("extra", StringArgumentType.greedyString())
                .executes(ctx -> {
                    sourceTipExact(ctx.getSource(), canonical);
                    return 0;
                });
    }

    private static void sourceTipExact(CommandSourceStack source, String canonical) {
        source.sendFailure(Component.literal(
                "Almost — Eva Mod does not take extra words after the command."));
        if ("locate".equals(canonical)) {
            source.sendSuccess(() -> line(
                    Component.literal("Try: ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod locate", "Find next house/town"),
                    Component.literal(" or ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod locate reset", "Clear locate skip list")), false);
            return;
        }
        if ("town".equals(canonical)) {
            source.sendSuccess(() -> line(
                    Component.literal("Try: ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod town", "Find nearest town"),
                    Component.literal(" or ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod town visit", "Teleport into town house")), false);
            return;
        }
        source.sendSuccess(() -> line(
                Component.literal("Try exactly: ").withStyle(ChatFormatting.GRAY),
                suggestCommand("/evamod " + canonical, "Run /evamod " + canonical)), false);
    }

    private static int unknownInput(CommandSourceStack source, String raw) {
        String cleaned = normalizeToken(raw);
        if (cleaned.isEmpty()) {
            return help(source);
        }
        String[] parts = cleaned.split("\\s+");
        String head = parts[0];
        String canonical = COMMAND_ALIASES.get(head);
        if (canonical != null) {
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
        if ("town".equals(canonical) && parts.length >= 2
                && (parts[1].equals("visit") || parts[1].equals("tp") || parts[1].equals("goto"))) {
            source.sendSuccess(() -> line(
                    Component.literal("Try exactly: ").withStyle(ChatFormatting.GRAY),
                    suggestCommand("/evamod town visit", "Teleport into town house")), false);
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

    private static boolean structuresAvailable(ServerLevel level, boolean townsOnly) {
        try {
            var lookup = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            if (townsOnly) {
                lookup.getOrThrow(NPC_TOWN);
            } else {
                lookup.getOrThrow(NPC_HOUSE);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String normalizeToken(String raw) {
        if (raw == null) {
            return "";
        }
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
        return s.replaceAll("\\s+", " ");
    }

    private static String closestCommand(String input) {
        String best = null;
        int bestDist = Integer.MAX_VALUE;
        for (String cmd : KNOWN_COMMANDS) {
            int dist = levenshtein(input, cmd);
            int max = Math.max(1, cmd.length() / 3);
            if (dist <= max && dist < bestDist) {
                bestDist = dist;
                best = cmd;
            }
        }
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
