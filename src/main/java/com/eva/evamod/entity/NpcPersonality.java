package com.eva.evamod.entity;

import net.minecraft.util.RandomSource;

/**
 * Each NPC rolls one personality at spawn. It colors their dialogue, how
 * eager they are to socialize, whether they nap on the job, and the little
 * particles they idle with.
 */
public enum NpcPersonality {
    CHEERFUL("Cheerful"),
    SHY("Shy"),
    DRAMATIC("Dramatic"),
    GRUMPY("Grumpy"),
    SLEEPY("Sleepy"),
    CURIOUS("Curious"),
    KIND("Kind"),
    WITTY("Witty"),
    MYSTERIOUS("Mysterious"),
    BRAVE("Brave");

    private final String displayName;

    NpcPersonality(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** How often (roughly) this NPC seeks out a neighbor for a chat. Lower = more social. */
    public int socializeRarity() {
        return switch (this) {
            case CHEERFUL, CURIOUS, KIND, WITTY -> 400;
            case DRAMATIC, BRAVE -> 600;
            case GRUMPY, SLEEPY, MYSTERIOUS -> 1600;
            case SHY -> 1200;
        };
    }

    public boolean takesNaps() {
        return this == SLEEPY;
    }

    public static NpcPersonality byId(int id) {
        NpcPersonality[] values = values();
        return values[Math.floorMod(id, values.length)];
    }

    public static NpcPersonality random(RandomSource random) {
        NpcPersonality[] values = values();
        return values[random.nextInt(values.length)];
    }
}
