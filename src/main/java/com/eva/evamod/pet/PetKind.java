package com.eva.evamod.pet;

import java.util.Locale;
import net.minecraft.util.RandomSource;

/**
 * Immortal stuffed-animal kinds that can awaken into helpful companions.
 */
public enum PetKind {
    TEDDY("Teddy", "teddy", new String[]{
            "Muffin", "Button", "Cocoa", "Pudding", "Biscuit", "Noodle", "Maple", "Toast"}),
    BUNNY("Bunny", "bunny", new String[]{
            "Clover", "Thumper", "Cotton", "Peony", "Hopscotch", "Velvet", "Daisy", "Nibble"}),
    FOX("Fox", "fox", new String[]{
            "Ember", "Russet", "Pepper", "Maple", "Flick", "Copper", "Sable", "Whisper"}),
    CAT("Cat", "cat", new String[]{
            "Miso", "Purrl", "Socks", "Mochi", "Nimbus", "Velvet", "Pebble", "Whiskers"}),
    DRAGON("Dragon", "dragon", new String[]{
            "Emberkin", "Spark", "Puff", "Scale", "Nugget", "Cinder", "Wisp", "Snugglewyrm"}),
    OWL("Owl", "owl", new String[]{
            "Hoot", "Twilight", "Feather", "Atlas", "Lumen", "Sage", "Nook", "Moonbeam"}),
    FROG("Frog", "frog", new String[]{
            "Lilypad", "Ribbit", "Moss", "Pond", "Pickle", "Bounce", "Dew", "Croak"}),
    SHEEP("Sheep", "sheep", new String[]{
            "Cloud", "Fluff", "Woolly", "Marshmallow", "Puffball", "Lambkin", "Softie", "Cream"});

    private final String displayName;
    private final String textureSuffix;
    private final String[] defaultNames;

    PetKind(String displayName, String textureSuffix, String[] defaultNames) {
        this.displayName = displayName;
        this.textureSuffix = textureSuffix;
        this.defaultNames = defaultNames;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTextureSuffix() {
        return textureSuffix;
    }

    public String[] getDefaultNames() {
        return defaultNames;
    }

    public String randomName(RandomSource random) {
        return defaultNames[random.nextInt(defaultNames.length)];
    }

    public static PetKind byId(int id) {
        PetKind[] values = values();
        return values[Math.floorMod(id, values.length)];
    }

    public static PetKind byName(String name) {
        if (name == null || name.isBlank()) {
            return TEDDY;
        }
        try {
            return PetKind.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return TEDDY;
        }
    }

    public static PetKind random(RandomSource random) {
        PetKind[] values = values();
        return values[random.nextInt(values.length)];
    }
}
