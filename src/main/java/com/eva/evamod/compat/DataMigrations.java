package com.eva.evamod.compat;

import com.eva.evamod.EvaMod;
import com.eva.evamod.ModVersions;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.world.EvaWorldData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Idempotent, O(1) schema migrations. Safe to call on every login — no world scans.
 */
public final class DataMigrations {
    private DataMigrations() {
    }

    public static PlayerEvaData ensurePlayer(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.schemaVersion() >= ModVersions.PLAYER_SCHEMA) {
            return data;
        }
        int from = data.schemaVersion();
        data.setSchemaVersion(ModVersions.PLAYER_SCHEMA);
        // schema 0/1 → 2: guide-book flag defaults false (already).
        // schema 2 → 3: optional pet + adventure/landmark lists default empty.
        // 3.1.0: network 4 for pet menu; NPC gender is entity NBT (optional Int Gender).
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        if (from < ModVersions.PLAYER_SCHEMA) {
            EvaMod.LOGGER.debug("Migrated player {} Eva data schema {} → {}",
                    player.getGameProfile().name(), from, ModVersions.PLAYER_SCHEMA);
        }
        return player.getData(ModAttachments.PLAYER_DATA);
    }

    public static EvaWorldData ensureWorld(ServerLevel level) {
        EvaWorldData data = level.getData(ModAttachments.WORLD_DATA);
        long day = level.getOverworldClockTime() / 24000L;
        boolean dirty = false;
        if (data.installedDay() < 0) {
            data.setInstalledDay(day);
            dirty = true;
        }
        if (data.schemaVersion() < ModVersions.WORLD_SCHEMA) {
            int from = data.schemaVersion();
            data.setSchemaVersion(ModVersions.WORLD_SCHEMA);
            dirty = true;
            EvaMod.LOGGER.info("Migrated overworld Eva data schema {} → {}", from, ModVersions.WORLD_SCHEMA);
        }
        // Heuristic: if the world clock is already far along when we first install, mark legacy.
        // Does not scan chunks — just notes that natural structures may be far/unexplored.
        if (!data.likelyLegacyWorld() && data.installedDay() >= 0 && day >= 30 && !data.founderHomesteadPlaced()) {
            data.setLikelyLegacyWorld(true);
            dirty = true;
        }
        if (dirty) {
            level.setData(ModAttachments.WORLD_DATA, data.copy());
        }
        return level.getData(ModAttachments.WORLD_DATA);
    }
}
