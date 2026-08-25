package com.eva.evamod.entity;

/**
 * Presentation gender for biome NPCs — drives silhouette/skin choice and name pools.
 * Folk-fantasy presentation (hair length, tunic vs belt), not a gameplay stat.
 */
public enum NpcGender {
    FEMALE("f"),
    MALE("m");

    private final String textureSuffix;

    NpcGender(String textureSuffix) {
        this.textureSuffix = textureSuffix;
    }

    public String getTextureSuffix() {
        return textureSuffix;
    }

    public static NpcGender byId(int id) {
        NpcGender[] values = values();
        return values[Math.floorMod(id, values.length)];
    }

    public static NpcGender random(net.minecraft.util.RandomSource random) {
        return random.nextBoolean() ? FEMALE : MALE;
    }
}
