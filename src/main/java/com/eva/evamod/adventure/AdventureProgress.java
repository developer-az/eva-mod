package com.eva.evamod.adventure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** Persisted progress for one adventure chain. */
public record AdventureProgress(
        String id,
        int stepIndex,
        boolean completed,
        int counter) {

    public static final Codec<AdventureProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(AdventureProgress::id),
            Codec.INT.fieldOf("step").forGetter(AdventureProgress::stepIndex),
            Codec.BOOL.fieldOf("done").forGetter(AdventureProgress::completed),
            Codec.INT.optionalFieldOf("counter", 0).forGetter(AdventureProgress::counter)
    ).apply(instance, AdventureProgress::new));

    public static final Codec<List<AdventureProgress>> LIST_CODEC = CODEC.listOf();

    public AdventureProgress withStep(int step, int counter) {
        return new AdventureProgress(id, step, false, counter);
    }

    public AdventureProgress markDone() {
        return new AdventureProgress(id, stepIndex, true, counter);
    }
}
