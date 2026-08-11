package com.eva.evamod.adventure;

/**
 * One step in a multi-part adventure. {@code target} is a threshold or enum name
 * depending on {@link AdventureStepType}; {@code hint} is player-facing copy.
 */
public record AdventureStep(AdventureStepType type, String target, int amount, String hint) {
    public AdventureStep(AdventureStepType type, int amount, String hint) {
        this(type, "", amount, hint);
    }

    public AdventureStep(AdventureStepType type, String target, String hint) {
        this(type, target, 1, hint);
    }
}
