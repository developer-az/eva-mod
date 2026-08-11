package com.eva.evamod.adventure;

import java.util.List;

/**
 * Static adventure definition. Content is code-authored for 3.0 — schema stays stable
 * via optional progress fields on the player attachment.
 */
public record AdventureDef(
        String id,
        String title,
        String summary,
        List<AdventureStep> steps,
        int rewardRepHint,
        String rewardBlurb) {

    public int stepCount() {
        return steps.size();
    }

    public AdventureStep step(int index) {
        if (index < 0 || index >= steps.size()) {
            return null;
        }
        return steps.get(index);
    }
}
