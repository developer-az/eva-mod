package com.eva.evamod.world;

import com.eva.evamod.entity.NpcNames;
import com.eva.evamod.entity.NpcVariant;
import com.eva.evamod.registry.ModAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;

/** World-scoped set of claimed NPC display names so houses never share a name. */
public class UsedNpcNamesData {
    public static final Codec<UsedNpcNamesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("used", java.util.List.of()).forGetter(d -> d.used.stream().sorted().toList())
    ).apply(instance, UsedNpcNamesData::fromList));

    private static final String[] ROMAN = {
            "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
            "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
    };

    private final Set<String> used;

    public UsedNpcNamesData() {
        this.used = new HashSet<>();
    }

    private UsedNpcNamesData(Set<String> used) {
        this.used = used;
    }

    private static UsedNpcNamesData fromList(java.util.List<String> names) {
        Set<String> set = new HashSet<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                set.add(normalize(name));
            }
        }
        return new UsedNpcNamesData(set);
    }

    public static String claim(LevelAccessor level, NpcVariant variant, RandomSource random) {
        ServerLevel serverLevel = resolveServerLevel(level);
        if (serverLevel == null) {
            return NpcNames.pick(variant, random);
        }
        UsedNpcNamesData data = serverLevel.getData(ModAttachments.USED_NPC_NAMES);
        String name = data.claimUnique(variant, random);
        serverLevel.setData(ModAttachments.USED_NPC_NAMES, data.copy());
        return name;
    }

    private static ServerLevel resolveServerLevel(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }
        if (level instanceof WorldGenLevel worldGenLevel) {
            return worldGenLevel.getLevel();
        }
        return null;
    }

    public String claimUnique(NpcVariant variant, RandomSource random) {
        String picked = NpcNames.pickUnused(variant, random, used);
        if (picked != null) {
            used.add(normalize(picked));
            return picked;
        }
        picked = NpcNames.pickUnusedAny(random, used);
        if (picked != null) {
            used.add(normalize(picked));
            return picked;
        }
        String base = NpcNames.pick(variant, random);
        for (String suffix : ROMAN) {
            String candidate = base + " " + suffix;
            if (used.add(normalize(candidate))) {
                return candidate;
            }
        }
        for (int n = 21; n < 10_000; n++) {
            String candidate = base + " " + n;
            if (used.add(normalize(candidate))) {
                return candidate;
            }
        }
        String fallback = base + " " + Long.toHexString(random.nextLong());
        used.add(normalize(fallback));
        return fallback;
    }

    public UsedNpcNamesData copy() {
        return new UsedNpcNamesData(new HashSet<>(used));
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
