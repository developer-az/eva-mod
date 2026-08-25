package com.eva.evamod.pet;

import java.util.Locale;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

/**
 * Immortal stuffed-animal kinds that can awaken into helpful companions.
 * Each kind has its own soft sound identity (never combat).
 */
public enum PetKind {
    TEDDY("Teddy", "teddy", 0.58F, 0.85F,
            new String[]{"Muffin", "Button", "Cocoa", "Pudding", "Biscuit", "Noodle", "Maple", "Toast"}),
    BUNNY("Bunny", "bunny", 0.52F, 1.35F,
            new String[]{"Clover", "Thumper", "Cotton", "Peony", "Hopscotch", "Velvet", "Daisy", "Nibble"}),
    FOX("Fox", "fox", 0.54F, 1.15F,
            new String[]{"Ember", "Russet", "Pepper", "Maple", "Flick", "Copper", "Sable", "Whisper"}),
    CAT("Cat", "cat", 0.50F, 1.45F,
            new String[]{"Miso", "Purrl", "Socks", "Mochi", "Nimbus", "Velvet", "Pebble", "Whiskers"}),
    DRAGON("Dragon", "dragon", 0.62F, 0.75F,
            new String[]{"Emberkin", "Spark", "Puff", "Scale", "Nugget", "Cinder", "Wisp", "Snugglewyrm"}),
    OWL("Owl", "owl", 0.55F, 0.95F,
            new String[]{"Hoot", "Twilight", "Feather", "Atlas", "Lumen", "Sage", "Nook", "Moonbeam"}),
    FROG("Frog", "frog", 0.48F, 1.25F,
            new String[]{"Lilypad", "Ribbit", "Moss", "Pond", "Pickle", "Bounce", "Dew", "Croak"}),
    SHEEP("Sheep", "sheep", 0.56F, 1.05F,
            new String[]{"Cloud", "Fluff", "Woolly", "Marshmallow", "Puffball", "Lambkin", "Softie", "Cream"});

    private final String displayName;
    private final String textureSuffix;
    private final float renderScale;
    private final float voicePitch;
    private final String[] defaultNames;

    PetKind(String displayName, String textureSuffix, float renderScale, float voicePitch, String[] defaultNames) {
        this.displayName = displayName;
        this.textureSuffix = textureSuffix;
        this.renderScale = renderScale;
        this.voicePitch = voicePitch;
        this.defaultNames = defaultNames;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTextureSuffix() {
        return textureSuffix;
    }

    public float getRenderScale() {
        return renderScale;
    }

    public float getVoicePitch() {
        return voicePitch;
    }

    public SoundEvent ambientSound() {
        return switch (this) {
            case BUNNY -> SoundEvents.RABBIT_AMBIENT;
            case FOX -> SoundEvents.FOX_AMBIENT;
            case CAT -> SoundEvents.CAT_PURR_BABY.value();
            case FROG -> SoundEvents.FROG_AMBIENT;
            case SHEEP -> SoundEvents.SHEEP_AMBIENT;
            case OWL -> SoundEvents.PARROT_AMBIENT;
            case DRAGON -> SoundEvents.ALLAY_AMBIENT_WITH_ITEM;
            case TEDDY -> SoundEvents.PANDA_AMBIENT;
        };
    }

    public SoundEvent hurtSound() {
        return switch (this) {
            case BUNNY -> SoundEvents.RABBIT_HURT;
            case FOX -> SoundEvents.FOX_HURT;
            case CAT -> SoundEvents.CAT_HURT_BABY.value();
            case FROG -> SoundEvents.FROG_HURT;
            case SHEEP -> SoundEvents.SHEEP_HURT;
            case OWL -> SoundEvents.PARROT_HURT;
            case DRAGON -> SoundEvents.ALLAY_HURT;
            case TEDDY -> SoundEvents.PANDA_HURT;
        };
    }

    public SoundEvent deathSound() {
        return switch (this) {
            case BUNNY -> SoundEvents.RABBIT_DEATH;
            case FOX -> SoundEvents.FOX_DEATH;
            case CAT -> SoundEvents.CAT_DEATH_BABY.value();
            case FROG -> SoundEvents.FROG_DEATH;
            case SHEEP -> SoundEvents.SHEEP_DEATH;
            case OWL -> SoundEvents.PARROT_DEATH;
            case DRAGON -> SoundEvents.ALLAY_DEATH;
            case TEDDY -> SoundEvents.PANDA_DEATH;
        };
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
