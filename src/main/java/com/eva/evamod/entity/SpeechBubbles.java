package com.eva.evamod.entity;

import net.minecraft.util.RandomSource;

/** Short cute floating lines. Kept tiny to avoid chat/lag spam. */
public final class SpeechBubbles {
    public enum Kind {
        IDLE_WORK,
        PLAYER_NEAR,
        GIFT,
        SOCIAL,
        WEATHER
    }

    private static final String[] IDLE_WORK = {
            "Hmm…", "Just a bit more…", "Nice and steady.", "Almost tidy!", "Worky work~", "Hmm!"
    };
    private static final String[] PLAYER_NEAR = {
            "Oh! Hi!", "Welcome!", "Hello there~", "Need anything?", "Hey friend!"
    };
    private static final String[] GIFT = {
            "For me?!", "Aww!", "Thank you!", "You're sweet!", "Yay!"
    };
    private static final String[] SOCIAL = {
            "Really?", "No way!", "Hehe~", "Tell me more!", "Same here!"
    };
    private static final String[] WEATHER = {
            "Brr…", "Rain again?", "Cozy weather.", "Dash home!", "Wet socks…"
    };

    public static String pick(Kind kind, RandomSource random) {
        String[] pool = switch (kind) {
            case IDLE_WORK -> IDLE_WORK;
            case PLAYER_NEAR -> PLAYER_NEAR;
            case GIFT -> GIFT;
            case SOCIAL -> SOCIAL;
            case WEATHER -> WEATHER;
        };
        return pool[random.nextInt(pool.length)];
    }

    private SpeechBubbles() {
    }
}
