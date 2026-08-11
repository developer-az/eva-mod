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
        NAMES.put(NpcVariant.OCEAN, new String[]{
                "Marin", "Coral", "Tide", "Pearl", "Kai", "Nerida", "Brine", "Shelly", "Wave", "Anchor",
                "Delta", "Siren", "Reef", "Salten", "Marina", "Cove", "Drift", "Kelp", "Harbor", "Foam",
                "Nori", "Lumen", "Spray", "Captain", "Isla", "Bay", "Current", "Pike", "Oyster", "Finn",
                "Aqua", "Merrow", "Salty", "Lagoon", "Wharf", "Nautilus"});
        NAMES.put(NpcVariant.CHERRY, new String[]{
                "Sakura", "Hanami", "Blossom", "Petal", "Momo", "Yuki", "Haru", "Aya", "Ren", "Kohana",
                "Sora", "Hana", "Kiku", "Mei", "Tsubaki", "Akira", "Nori", "Sumi", "Kaito", "Rin",
                "Pinkie", "Bloom", "Cherry", "Ume", "Asa", "Mio", "Kenji", "Fuji", "Saki", "Tomoe",
                "Lila", "Orchid", "Plum", "Spring", "Petunia", "Kaori"});
        NAMES.put(NpcVariant.BADLANDS, new String[]{
                "Dusty", "Clay", "Rust", "Mesa", "Canyon", "Cinder", "Adobe", "Redrock", "Sage", "Butch",
                "Tess", "Ridge", "Flint", "Sienna", "Outlaw", "Marl", "Terra", "Bluff", "Copper", "Dryden",
                "Sundance", "Raven", "Ochre", "Spur", "Cordova", "Pueblo", "Arroyo", "Grit", "Vega", "Bronco",
                "Sierra", "Quarry", "Tumble", "Ash", "Horizon", "Mesquite"});
        NAMES.put(NpcVariant.MUSHROOM, new String[]{
                "Mycel", "Shroom", "Morel", "Truffle", "Spore", "Cap", "Puff", "Gilly", "Mossy", "Fungus",
                "Dotty", "Amanita", "Button", "Chanter", "Lumen", "Softcap", "Nibble", "Wisp", "Umber", "Porto",
                "Glowcap", "Velvet", "Toadstool", "Inkcap", "Fairy", "Ringlet", "Dew", "Misty", "Boletus", "Piko",
                "Saffron", "Enoki", "Shiitake", "Polka", "Nimbus", "Sprout"});
        NAMES.put(NpcVariant.DARK, new String[]{
                "Shade", "Umbra", "Thorn", "Nightshade", "Raven", "Hollow", "Ebony", "Grim", "Ashen", "Nocturne",
                "Briar", "Vesper", "Crow", "Murkwood", "Silva", "Gloom", "Twilight", "Oakheart", "Rowan", "Sable",
                "Dusk", "Wraith", "Ivy", "Morwen", "Blackbriar", "Lurk", "Canopy", "Fernshadow", "Nyx", "Hedgerow",
                "Quiet", "Deepwood", "Mossbane", "Elder", "Root", "Whisper"});
        NAMES.put(NpcVariant.MOUNTAIN, new String[]{
                "Summit", "Cliff", "Granite", "Peak", "Alpine", "Stone", "Ridge", "Tor", "Echo", "Avalanche",
                "Boulder", "Crag", "Pike", "Helena", "Flint", "Cascade", "Sherpa", "Yeti", "Frostpeak", "Slate",
                "Kestrel", "Horn", "Mira", "Basalt", "Heather", "Windward", "Talus", "Ivo", "Sierra", "Quarry",
                "Hawk", "Glacier", "Berg", "Astra", "Cairn", "Skyline"});
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
