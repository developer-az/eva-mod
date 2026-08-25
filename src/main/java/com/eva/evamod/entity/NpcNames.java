package com.eva.evamod.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

/**
 * Gendered name pools per biome so presentation, voice of name, and skin agree.
 */
public final class NpcNames {
    private static final Map<NpcVariant, Map<NpcGender, String[]>> NAMES = new EnumMap<>(NpcVariant.class);

    static {
        put(NpcVariant.PLAINS,
                new String[]{"Wilma", "Greta", "Elsie", "Marta", "Poppy", "Hilda", "Nette", "Clara", "Anke", "Lotte",
                        "Mina", "Saskia", "Trudy", "Berta", "Fleur", "Dora", "Iris", "Heidi"},
                new String[]{"Otto", "Bram", "Tobin", "Cedric", "Alden", "Rolf", "Pieter", "Jonas", "Felix", "Gerrit",
                        "Hugo", "Emile", "Wessel", "Nils", "Karel", "Simon", "Pim", "Lars"});
        put(NpcVariant.DESERT,
                new String[]{"Zahra", "Naila", "Layla", "Amina", "Yasmin", "Salma", "Dalia", "Noor", "Leila", "Hana",
                        "Rana", "Soraya", "Amira", "Farah", "Safiya", "Lina", "Maya", "Zara"},
                new String[]{"Rashid", "Tarek", "Samir", "Farid", "Khalid", "Omar", "Hassan", "Karim", "Idris", "Yusuf",
                        "Malik", "Zain", "Bilal", "Nabil", "Jamal", "Adil", "Tariq", "Rami"});
        put(NpcVariant.TAIGA,
                new String[]{"Freya", "Ingrid", "Astrid", "Signe", "Helga", "Solveig", "Liv", "Kara", "Britta", "Ylva",
                        "Tove", "Runa", "Elin", "Sigrid", "Hanne", "Ida", "Maja", "Vera"},
                new String[]{"Björn", "Sten", "Leif", "Torvald", "Rurik", "Sven", "Erik", "Gunnar", "Olaf", "Ivar",
                        "Anders", "Magnus", "Harald", "Birger", "Ulf", "Knut", "Ragnar", "Folke"});
        put(NpcVariant.SNOWY,
                new String[]{"Eira", "Nuvia", "Tuula", "Sedna", "Ivalu", "Verna", "Kirima", "Tala", "Ilma", "Aila",
                        "Yara", "Sanna", "Lumi", "Kaia", "Elsa", "Nivi", "Petra", "Halla", "Sila", "Umi"},
                new String[]{"Kaspar", "Oleg", "Miska", "Anok", "Siku", "Aput", "Nuka", "Soren", "Pekka", "Torben",
                        "Mikkel", "Rune", "Arktou", "Oslo", "Frey", "Kai"});
        put(NpcVariant.SAVANNA,
                new String[]{"Amara", "Zuri", "Nia", "Ayana", "Imani", "Asha", "Zola", "Nala", "Sade", "Makena",
                        "Tema", "Kesi", "Zainab", "Lindiwe", "Eshe", "Binta", "Folami", "Nuru", "Amani", "Zahara"},
                new String[]{"Kofi", "Jabari", "Simba", "Tendai", "Bakari", "Kwame", "Tari", "Jelani", "Chidi", "Ayo",
                        "Dayo", "Obi", "Tau", "Kamau", "Sefu", "Jengo"});
        put(NpcVariant.JUNGLE,
                new String[]{"Itzel", "Kanti", "Maya", "Xoco", "Nima", "Lani", "Suki", "Iara", "Anahi", "Luz",
                        "Nayeli", "Saffron", "Zena", "Vira", "Sena", "Ria", "Cala"},
                new String[]{"Yuma", "Rio", "Tono", "Balam", "Coati", "Paco", "Quetz", "Tavi", "Momo", "Cielo",
                        "Ruka", "Tikal", "Orin", "Pax", "Koa", "Mango", "Tulum", "Ilan", "Bodi"});
        put(NpcVariant.SWAMP,
                new String[]{"Mirtle", "Willow", "Sable", "Rana", "Bruma", "Hazel", "Wisp", "Nettle", "Briar",
                        "Thistle", "Glimmer", "Wren", "Fern", "Ivy", "Fog"},
                new String[]{"Fenwick", "Boggs", "Croaker", "Moss", "Tadd", "Murk", "Reed", "Puddle", "Marsh", "Cinder",
                        "Frogge", "Lichen", "Dusk", "Mire", "Pebble", "Sedge", "Toad", "Slate", "Pond", "Cricket",
                        "Bramble"});
        put(NpcVariant.OCEAN,
                new String[]{"Coral", "Pearl", "Nerida", "Shelly", "Siren", "Marina", "Foam", "Isla", "Aqua",
                        "Lagoon", "Nori"},
                new String[]{"Marin", "Tide", "Kai", "Brine", "Wave", "Anchor", "Delta", "Reef", "Salten", "Cove",
                        "Drift", "Kelp", "Harbor", "Lumen", "Spray", "Captain", "Bay", "Current", "Pike", "Oyster",
                        "Finn", "Merrow", "Salty", "Wharf", "Nautilus"});
        put(NpcVariant.CHERRY,
                new String[]{"Sakura", "Hanami", "Blossom", "Petal", "Momo", "Yuki", "Aya", "Kohana", "Hana", "Kiku",
                        "Mei", "Tsubaki", "Sumi", "Rin", "Pinkie", "Bloom", "Cherry", "Ume", "Mio", "Saki", "Tomoe",
                        "Lila", "Orchid", "Petunia", "Kaori"},
                new String[]{"Haru", "Ren", "Sora", "Akira", "Nori", "Kaito", "Kenji", "Fuji", "Plum", "Spring", "Asa"});
        put(NpcVariant.BADLANDS,
                new String[]{"Tess", "Sienna", "Terra", "Raven", "Vega", "Sierra", "Ash"},
                new String[]{"Dusty", "Clay", "Rust", "Mesa", "Canyon", "Cinder", "Adobe", "Redrock", "Sage", "Butch",
                        "Ridge", "Flint", "Outlaw", "Marl", "Bluff", "Copper", "Dryden", "Sundance", "Ochre", "Spur",
                        "Cordova", "Pueblo", "Arroyo", "Grit", "Bronco", "Quarry", "Tumble", "Horizon", "Mesquite"});
        put(NpcVariant.MUSHROOM,
                new String[]{"Dotty", "Amanita", "Fairy", "Ringlet", "Dew", "Misty", "Polka", "Nimbus", "Sprout",
                        "Velvet"},
                new String[]{"Mycel", "Shroom", "Morel", "Truffle", "Spore", "Cap", "Puff", "Gilly", "Mossy", "Fungus",
                        "Button", "Chanter", "Lumen", "Softcap", "Nibble", "Wisp", "Umber", "Porto", "Glowcap",
                        "Toadstool", "Inkcap", "Boletus", "Piko", "Saffron", "Enoki", "Shiitake"});
        put(NpcVariant.DARK,
                new String[]{"Nightshade", "Silva", "Ivy", "Morwen", "Nyx", "Quiet", "Fernshadow"},
                new String[]{"Shade", "Umbra", "Thorn", "Raven", "Hollow", "Ebony", "Grim", "Ashen", "Nocturne",
                        "Briar", "Vesper", "Crow", "Murkwood", "Gloom", "Twilight", "Oakheart", "Rowan", "Sable",
                        "Dusk", "Wraith", "Blackbriar", "Lurk", "Canopy", "Hedgerow", "Deepwood", "Mossbane", "Elder",
                        "Root", "Whisper"});
        put(NpcVariant.MOUNTAIN,
                new String[]{"Helena", "Mira", "Heather", "Sierra", "Astra", "Cascade"},
                new String[]{"Summit", "Cliff", "Granite", "Peak", "Alpine", "Stone", "Ridge", "Tor", "Echo",
                        "Avalanche", "Boulder", "Crag", "Pike", "Flint", "Sherpa", "Yeti", "Frostpeak", "Slate",
                        "Kestrel", "Horn", "Basalt", "Windward", "Talus", "Ivo", "Quarry", "Hawk", "Glacier", "Berg",
                        "Cairn", "Skyline"});
    }

    private static void put(NpcVariant variant, String[] feminine, String[] masculine) {
        Map<NpcGender, String[]> byGender = new EnumMap<>(NpcGender.class);
        byGender.put(NpcGender.FEMALE, feminine);
        byGender.put(NpcGender.MALE, masculine);
        NAMES.put(variant, byGender);
    }

    public static String pick(NpcVariant variant, NpcGender gender, RandomSource random) {
        String[] pool = pool(variant, gender);
        return pool[random.nextInt(pool.length)];
    }

    public static String pick(NpcVariant variant, RandomSource random) {
        return pick(variant, NpcGender.random(random), random);
    }

    public static @Nullable String pickUnused(NpcVariant variant, NpcGender gender, RandomSource random,
                                              Set<String> usedNormalized) {
        String[] pool = pool(variant, gender);
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

    public static @Nullable String pickUnused(NpcVariant variant, RandomSource random, Set<String> usedNormalized) {
        return pickUnused(variant, NpcGender.random(random), random, usedNormalized);
    }

    public static @Nullable String pickUnusedAny(NpcGender gender, RandomSource random, Set<String> usedNormalized) {
        List<String> free = new ArrayList<>();
        for (Map<NpcGender, String[]> byGender : NAMES.values()) {
            for (String name : byGender.get(gender)) {
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

    public static @Nullable String pickUnusedAny(RandomSource random, Set<String> usedNormalized) {
        return pickUnusedAny(NpcGender.random(random), random, usedNormalized);
    }

    private static String[] pool(NpcVariant variant, NpcGender gender) {
        return NAMES.get(variant).get(gender);
    }

    private NpcNames() {
    }
}
