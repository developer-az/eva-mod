package com.eva.evamod.command;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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

@EventBusSubscriber(modid = EvaMod.MODID)
public final class EvaModCommands {
    public static final ResourceKey<Structure> NPC_HOUSE =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));
    public static final ResourceKey<Structure> NPC_TOWN =
            ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_town"));
    public static final TagKey<Structure> NPC_SETTLEMENT_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(EvaMod.MODID, "npc_house"));

    private static final double NEAR_RADIUS = 48.0;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(Commands.literal("evamod")
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("locate")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> locate(ctx.getSource()))
                        .then(Commands.literal("reset")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> locateReset(ctx.getSource()))))
                .then(Commands.literal("near")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> near(ctx.getSource())))
                .then(Commands.literal("houses")
                        .executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("index")
                        .executes(ctx -> houses(ctx.getSource())))
                .then(Commands.literal("tp")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> visit(ctx.getSource())))
                .then(Commands.literal("visit")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> visit(ctx.getSource())))
                .executes(ctx -> help(ctx.getSource())));
        dispatcher.register(Commands.literal("eva").redirect(root));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Eva Mod commands:").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("/evamod locate — next nearest house/town (skips found)"), false);
        source.sendSuccess(() -> Component.literal("/evamod locate reset — clear locate skip list"), false);
        source.sendSuccess(() -> Component.literal("/evamod near — nearby Biome NPCs"), false);
        source.sendSuccess(() -> Component.literal("/evamod houses — personal house index (met NPCs)"), false);
        source.sendSuccess(() -> Component.literal("/evamod visit — teleport to nearest house (op)"), false);
        return 1;
    }

    private static int locate(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        if (SettlementLocator.isSearching(player.getUUID())) {
            source.sendFailure(Component.literal("Already searching — wait for the current locate to finish."));
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
                            : " — multi-NPC hamlet").withStyle(ChatFormatting.GRAY));
                }
                return msg;
            }, false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("No settlement in loaded chunks — searching nearby world…")
                .withStyle(ChatFormatting.YELLOW), false);
        SettlementLocator.startSearch(player, data);
        return 1;
    }

    private static int locateReset(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        SettlementLocator.cancel(player.getUUID());
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        data.resetLocated();
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        source.sendSuccess(() -> Component.literal("Locate skip list cleared. House index kept."), false);
        return 1;
    }

    private static int near(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        AABB box = player.getBoundingBox().inflate(NEAR_RADIUS);
        List<BiomeNpc> npcs = player.level().getEntitiesOfClass(BiomeNpc.class, box, Entity::isAlive);
        npcs.sort(Comparator.comparingDouble(player::distanceToSqr));
        if (npcs.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No Biome NPCs within " + (int) NEAR_RADIUS + " blocks."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Nearby NPCs (" + npcs.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (BiomeNpc npc : npcs.stream().limit(12).toList()) {
            BlockPos pos = npc.blockPosition();
            String line = npc.getNpcName() + " — " + npc.getPersonality().getDisplayName()
                    + " " + npc.getJob().getDisplayName();
            source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(line + " @ "), pos), false);
        }
        return npcs.size();
    }

    private static int houses(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        List<HouseIndexEntry> houses = player.getData(ModAttachments.PLAYER_DATA).houses();
        if (houses.isEmpty()) {
            source.sendSuccess(() -> Component.literal("House index empty — talk to NPCs to meet them."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("House Index (" + houses.size() + "):").withStyle(ChatFormatting.GOLD), false);
        for (HouseIndexEntry entry : houses) {
            String label = entry.npcName() + " — " + entry.personality() + " " + entry.biome()
                    + " " + entry.job() + " @ ";
            source.sendSuccess(() -> SettlementLocator.clickablePos(Component.literal(label), entry.homePos()), false);
        }
        return houses.size();
    }

    private static int visit(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Players only."));
            return 0;
        }
        ServerLevel level = player.level();
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        SettlementLocator.LocateResult found = SettlementLocator.tryInstant(level, player.blockPosition(), data);
        if (found == null) {
            // Single cheap probe — never run the full grid sync on visit.
            BlockPos hit = level.findNearestMapStructure(NPC_SETTLEMENT_TAG, player.blockPosition(), 64, false);
            if (hit == null) {
                source.sendFailure(Component.literal(
                        "No npc_house/npc_town nearby. Try /evamod locate first."));
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
    }

    private EvaModCommands() {
    }
}
