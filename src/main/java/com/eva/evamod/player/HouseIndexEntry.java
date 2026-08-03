package com.eva.evamod.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record HouseIndexEntry(
        String npcName,
        String job,
        String personality,
        String biome,
        BlockPos homePos) {

    public static final Codec<HouseIndexEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("npcName").forGetter(HouseIndexEntry::npcName),
            Codec.STRING.fieldOf("job").forGetter(HouseIndexEntry::job),
            Codec.STRING.fieldOf("personality").forGetter(HouseIndexEntry::personality),
            Codec.STRING.fieldOf("biome").forGetter(HouseIndexEntry::biome),
            BlockPos.CODEC.fieldOf("homePos").forGetter(HouseIndexEntry::homePos)
    ).apply(instance, HouseIndexEntry::new));
}
