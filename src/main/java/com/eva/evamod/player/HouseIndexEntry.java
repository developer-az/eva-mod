package com.eva.evamod.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** Journal entry for a met NPC — includes hearts and birthday for the homestead journal. */
public record HouseIndexEntry(
        String npcName,
        String job,
        String personality,
        String biome,
        BlockPos homePos,
        int hearts,
        String birthday) {

    public static final Codec<HouseIndexEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("npcName").forGetter(HouseIndexEntry::npcName),
            Codec.STRING.fieldOf("job").forGetter(HouseIndexEntry::job),
            Codec.STRING.fieldOf("personality").forGetter(HouseIndexEntry::personality),
            Codec.STRING.fieldOf("biome").forGetter(HouseIndexEntry::biome),
            BlockPos.CODEC.fieldOf("homePos").forGetter(HouseIndexEntry::homePos),
            Codec.INT.optionalFieldOf("hearts", 0).forGetter(HouseIndexEntry::hearts),
            Codec.STRING.optionalFieldOf("birthday", "?").forGetter(HouseIndexEntry::birthday)
    ).apply(instance, HouseIndexEntry::new));
}
