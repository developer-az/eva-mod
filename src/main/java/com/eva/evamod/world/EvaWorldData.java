package com.eva.evamod.world;

import com.eva.evamod.ModVersions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

/**
 * Overworld-scoped Eva Mod state. Schema-versioned so future features can migrate cleanly.
 * Used for retroactive (pre-mod) worlds and once-only bootstrap work — never per-tick.
 */
public class EvaWorldData {
    public static final Codec<EvaWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", 0).forGetter(d -> d.schemaVersion),
            Codec.LONG.optionalFieldOf("installedDay", -1L).forGetter(d -> d.installedDay),
            Codec.BOOL.optionalFieldOf("founderPlaced", false).forGetter(d -> d.founderHomesteadPlaced),
            BlockPos.CODEC.optionalFieldOf("founderPos").forGetter(d -> Optional.ofNullable(d.founderHomesteadPos)),
            Codec.BOOL.optionalFieldOf("legacyWorld", false).forGetter(d -> d.likelyLegacyWorld)
    ).apply(instance, EvaWorldData::fromCodec));

    private int schemaVersion;
    private long installedDay;
    private boolean founderHomesteadPlaced;
    private BlockPos founderHomesteadPos;
    private boolean likelyLegacyWorld;

    public EvaWorldData() {
        this(ModVersions.WORLD_SCHEMA, -1L, false, null, false);
    }

    public EvaWorldData(
            int schemaVersion,
            long installedDay,
            boolean founderHomesteadPlaced,
            BlockPos founderHomesteadPos,
            boolean likelyLegacyWorld) {
        this.schemaVersion = schemaVersion;
        this.installedDay = installedDay;
        this.founderHomesteadPlaced = founderHomesteadPlaced;
        this.founderHomesteadPos = founderHomesteadPos == null ? null : founderHomesteadPos.immutable();
        this.likelyLegacyWorld = likelyLegacyWorld;
    }

    private static EvaWorldData fromCodec(
            int schema,
            long installedDay,
            boolean founderPlaced,
            Optional<BlockPos> founderPos,
            boolean legacy) {
        return new EvaWorldData(schema, installedDay, founderPlaced, founderPos.orElse(null), legacy);
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public long installedDay() {
        return installedDay;
    }

    public void setInstalledDay(long day) {
        if (this.installedDay < 0) {
            this.installedDay = day;
        }
    }

    public boolean founderHomesteadPlaced() {
        return founderHomesteadPlaced;
    }

    public BlockPos founderHomesteadPos() {
        return founderHomesteadPos;
    }

    public void markFounderHomestead(BlockPos pos) {
        this.founderHomesteadPlaced = true;
        this.founderHomesteadPos = pos.immutable();
    }

    public boolean likelyLegacyWorld() {
        return likelyLegacyWorld;
    }

    public void setLikelyLegacyWorld(boolean likelyLegacyWorld) {
        this.likelyLegacyWorld = likelyLegacyWorld;
    }

    public EvaWorldData copy() {
        return new EvaWorldData(schemaVersion, installedDay, founderHomesteadPlaced, founderHomesteadPos, likelyLegacyWorld);
    }
}
