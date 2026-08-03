package com.eva.evamod.entity;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

public final class NpcNames {
    private static final Map<NpcVariant, String[]> NAMES = new EnumMap<>(NpcVariant.class);

    static {
        NAMES.put(NpcVariant.PLAINS, new String[]{
                "Wilma", "Otto", "Greta", "Bram", "Elsie", "Tobin", "Marta", "Cedric", "Poppy", "Alden"});
        NAMES.put(NpcVariant.DESERT, new String[]{
                "Zahra", "Rashid", "Naila", "Tarek", "Samir", "Layla", "Farid", "Amina", "Khalid", "Yasmin"});
        NAMES.put(NpcVariant.TAIGA, new String[]{
                "Björn", "Freya", "Sten", "Ingrid", "Leif", "Astrid", "Torvald", "Signe", "Rurik", "Helga"});
        NAMES.put(NpcVariant.SNOWY, new String[]{
                "Eira", "Kaspar", "Nuvia", "Oleg", "Tuula", "Miska", "Sedna", "Ivalu", "Anok", "Verna"});
        NAMES.put(NpcVariant.SAVANNA, new String[]{
                "Amara", "Kofi", "Zuri", "Jabari", "Nia", "Simba", "Ayana", "Tendai", "Imani", "Bakari"});
        NAMES.put(NpcVariant.JUNGLE, new String[]{
                "Itzel", "Yuma", "Kanti", "Rio", "Maya", "Tono", "Xoco", "Balam", "Nima", "Coati"});
        NAMES.put(NpcVariant.SWAMP, new String[]{
                "Mirtle", "Fenwick", "Boggs", "Willow", "Croaker", "Sable", "Moss", "Tadd", "Rana", "Murk"});
    }

    public static String pick(NpcVariant variant, RandomSource random) {
        String[] pool = NAMES.get(variant);
        return pool[random.nextInt(pool.length)];
    }

    private NpcNames() {
    }
}
