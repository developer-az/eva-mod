package com.eva.evamod.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

public final class NpcNames {
    private static final Map<NpcVariant, String[]> NAMES = new EnumMap<>(NpcVariant.class);

    static {
        NAMES.put(NpcVariant.PLAINS, new String[]{
                "Wilma", "Otto", "Greta", "Bram", "Elsie", "Tobin", "Marta", "Cedric", "Poppy", "Alden",
                "Hilda", "Rolf", "Nette", "Pieter", "Clara", "Jonas", "Anke", "Felix", "Lotte", "Gerrit",
                "Mina", "Hugo", "Saskia", "Emile", "Trudy", "Wessel", "Berta", "Nils", "Fleur", "Karel",
                "Dora", "Simon", "Iris", "Pim", "Heidi", "Lars"});
        NAMES.put(NpcVariant.DESERT, new String[]{
                "Zahra", "Rashid", "Naila", "Tarek", "Samir", "Layla", "Farid", "Amina", "Khalid", "Yasmin",
                "Omar", "Salma", "Hassan", "Dalia", "Karim", "Noor", "Idris", "Leila", "Yusuf", "Hana",
                "Malik", "Rana", "Zain", "Soraya", "Bilal", "Amira", "Nabil", "Farah", "Jamal", "Safiya",
                "Adil", "Lina", "Tariq", "Maya", "Rami", "Zara"});
        NAMES.put(NpcVariant.TAIGA, new String[]{
                "Björn", "Freya", "Sten", "Ingrid", "Leif", "Astrid", "Torvald", "Signe", "Rurik", "Helga",
                "Sven", "Solveig", "Erik", "Liv", "Gunnar", "Kara", "Olaf", "Britta", "Ivar", "Ylva",
                "Anders", "Tove", "Magnus", "Runa", "Harald", "Elin", "Birger", "Sigrid", "Ulf", "Hanne",
                "Knut", "Ida", "Ragnar", "Maja", "Folke", "Vera"});
        NAMES.put(NpcVariant.SNOWY, new String[]{
                "Eira", "Kaspar", "Nuvia", "Oleg", "Tuula", "Miska", "Sedna", "Ivalu", "Anok", "Verna",
                "Siku", "Aput", "Kirima", "Nuka", "Tala", "Soren", "Ilma", "Pekka", "Aila", "Torben",
                "Yara", "Mikkel", "Sanna", "Lumi", "Kaia", "Rune", "Elsa", "Arktou", "Nivi", "Petra",
                "Oslo", "Frey", "Halla", "Kai", "Umi", "Sila"});
        NAMES.put(NpcVariant.SAVANNA, new String[]{
                "Amara", "Kofi", "Zuri", "Jabari", "Nia", "Simba", "Ayana", "Tendai", "Imani", "Bakari",
                "Asha", "Kwame", "Zola", "Tari", "Nala", "Jelani", "Sade", "Chidi", "Ayo", "Makena",
                "Tema", "Dayo", "Kesi", "Obi", "Zainab", "Tau", "Lindiwe", "Eshe", "Binta", "Kamau",
                "Folami", "Sefu", "Nuru", "Amani", "Zahara", "Jengo"});
        NAMES.put(NpcVariant.JUNGLE, new String[]{
                "Itzel", "Yuma", "Kanti", "Rio", "Maya", "Tono", "Xoco", "Balam", "Nima", "Coati",
                "Lani", "Paco", "Suki", "Quetz", "Iara", "Tavi", "Momo", "Cielo", "Ruka", "Anahi",
                "Tikal", "Luz", "Orin", "Nayeli", "Pax", "Saffron", "Koa", "Zena", "Mango", "Vira",
                "Tulum", "Sena", "Ilan", "Ria", "Bodi", "Cala"});
        NAMES.put(NpcVariant.SWAMP, new String[]{
                "Mirtle", "Fenwick", "Boggs", "Willow", "Croaker", "Sable", "Moss", "Tadd", "Rana", "Murk",
                "Reed", "Bruma", "Puddle", "Hazel", "Marsh", "Wisp", "Cinder", "Frogge", "Lichen", "Nettle",
                "Dusk", "Briar", "Mire", "Thistle", "Pebble", "Glimmer", "Sedge", "Wren", "Toad", "Fern",
                "Slate", "Pond", "Ivy", "Cricket", "Fog", "Bramble"});
    }

    public static String pick(NpcVariant variant, RandomSource random) {
        String[] pool = NAMES.get(variant);
        return pool[random.nextInt(pool.length)];
    }

    public static @Nullable String pickUnused(NpcVariant variant, RandomSource random, Set<String> usedNormalized) {
        String[] pool = NAMES.get(variant);
        List<String> free = new ArrayList<>();
        for (String name : pool) {
            if (!usedNormalized.contains(name.toLowerCase(Locale.ROOT))) {
                free.add(name);
            }
        }
        if (free.isEmpty()) {
            return null;
        }
        return free.get(random.nextInt(free.size()));
    }

    public static @Nullable String pickUnusedAny(RandomSource random, Set<String> usedNormalized) {
        List<String> free = new ArrayList<>();
        for (String[] pool : NAMES.values()) {
            for (String name : pool) {
                if (!usedNormalized.contains(name.toLowerCase(Locale.ROOT))) {
                    free.add(name);
                }
            }
        }
        if (free.isEmpty()) {
            return null;
        }
        return free.get(random.nextInt(free.size()));
    }

    private NpcNames() {
    }
}
