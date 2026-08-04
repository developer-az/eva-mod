package com.eva.evamod.friendship;

import net.minecraft.util.Mth;

/**
 * Friendship hearts (0–10), Stardew-style. Backed by reputation (-100..100).
 * Strangers start near 0 hearts; close friends reach 10.
 */
public final class Hearts {
    public static final int MAX = 10;

    private Hearts() {
    }

    /** Map reputation to hearts. -100 → 0, 0 → 5, +100 → 10. */
    public static int fromReputation(int reputation) {
        return Mth.clamp((reputation + 100) / 20, 0, MAX);
    }

    public static String bar(int hearts) {
        int filled = Mth.clamp(hearts, 0, MAX);
        StringBuilder sb = new StringBuilder(MAX + 4);
        for (int i = 0; i < MAX; i++) {
            sb.append(i < filled ? '\u2665' : '-');
        }
        return sb.toString();
    }

    public static String shortLabel(int hearts) {
        return hearts + "/" + MAX + " hearts";
    }

    /** Heart-event milestones that unlock unique dialogue once each. */
    public static boolean isMilestone(int hearts) {
        return hearts == 2 || hearts == 4 || hearts == 6 || hearts == 8 || hearts == 10;
    }
}
