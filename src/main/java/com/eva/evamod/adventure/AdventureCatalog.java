package com.eva.evamod.adventure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Production adventure catalog for Companions 3.0.
 * Multi-step cozy stories — no combat, hooks into pets / NPCs / discovery.
 */
public final class AdventureCatalog {
    private static final Map<String, AdventureDef> BY_ID = new LinkedHashMap<>();

    static {
        register(new AdventureDef(
                "first_steps",
                "First Steps",
                "Learn the homestead loop: meet neighbors, find a house, keep a journal.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_NPCS, 1, "Right-click a Biome Villager to meet them."),
                        new AdventureStep(AdventureStepType.OPEN_JOURNAL, 1, "Open /evamod journal to see friends."),
                        new AdventureStep(AdventureStepType.LOCATE_SETTLEMENTS, 1, "Run /evamod locate to find a house.")
                ),
                5,
                "You know the basics — the world feels a little friendlier."));

        register(new AdventureDef(
                "soft_companion",
                "Soft Companion",
                "Awaken a stuffed animal and learn its gentle utilities.",
                List.of(
                        new AdventureStep(AdventureStepType.AWAKEN_PET, 1, "Place an Alive Plush on the ground to awaken a pet."),
                        new AdventureStep(AdventureStepType.PET_GLOW, 1, "Use /evamod pet glow or feed a Glow Berry."),
                        new AdventureStep(AdventureStepType.PET_FIND, 1, "Ask your pet to find a settlement (/evamod pet find).")
                ),
                8,
                "Your plush friend hums with quiet magic."));

        register(new AdventureDef(
                "lost_stitches",
                "Lost Stitches",
                "A Storyteller remembers toys that walked — help them gather the tale.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_JOB, "STORYTELLER", "Meet a Storyteller (Cherry, Ocean, Mushroom, or Dark Forest folk)."),
                        new AdventureStep(AdventureStepType.AWAKEN_PET, 1, "Awaken any stuffed pet if you have not already."),
                        new AdventureStep(AdventureStepType.PET_FIND, 1, "Have your pet lead you toward a settlement."),
                        new AdventureStep(AdventureStepType.COMPLETE_ERRANDS, 1, "Finish one Help errand for any friend.")
                ),
                12,
                "The Storyteller writes your pet into the village lore."));

        register(new AdventureDef(
                "cartographers_trail",
                "Cartographer's Trail",
                "Map the neighborhood — houses, then a true town.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_JOB, "CARTOGRAPHER", "Meet a Cartographer."),
                        new AdventureStep(AdventureStepType.LOCATE_SETTLEMENTS, 3, "Locate three settlements (/evamod locate or /evamod town)."),
                        new AdventureStep(AdventureStepType.DISCOVER_TOWN, 1, "Discover an npc_town hamlet.")
                ),
                10,
                "Your mental map of Eva homes grows denser."));

        register(new AdventureDef(
                "heart_of_the_hamlet",
                "Heart of the Hamlet",
                "Become the neighbor everyone relies on.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_NPCS, 3, "Meet three different Biome Villagers."),
                        new AdventureStep(AdventureStepType.COMPLETE_ERRANDS, 3, "Complete three Help errands."),
                        new AdventureStep(AdventureStepType.GIFT_NPC, 1, "Sneak + right-click to gift a favorite treat.")
                ),
                15,
                "Word spreads — you are part of the hamlet now."));

        register(new AdventureDef(
                "seasonal_wanderer",
                "Seasonal Wanderer",
                "Live by the Eva calendar: seasons, letters, small kindnesses.",
                List.of(
                        new AdventureStep(AdventureStepType.CHECK_CALENDAR, 1, "Check /evamod calendar."),
                        new AdventureStep(AdventureStepType.GIFT_NPC, 1, "Bring a gift to someone you know."),
                        new AdventureStep(AdventureStepType.READ_MAIL, 1, "Read your mail (/evamod mail).")
                ),
                8,
                "You keep time with festivals and friendly letters."));

        register(new AdventureDef(
                "ocean_lullaby",
                "Ocean Lullaby",
                "Meet the shore folk and share the quiet of the tide.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "OCEAN", "Meet an Ocean Biome Villager (beaches & coasts)."),
                        new AdventureStep(AdventureStepType.COMPLETE_ERRANDS, 1, "Help them with one errand."),
                        new AdventureStep(AdventureStepType.GIFT_NPC, 1, "Leave a small gift by the water.")
                ),
                10,
                "Waves and lullabies follow you inland."));

        register(new AdventureDef(
                "mushroom_whispers",
                "Mushroom Whispers",
                "Soft voices under red caps — the mushroom folk have stories.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "MUSHROOM", "Meet a Mushroom Fields neighbor."),
                        new AdventureStep(AdventureStepType.MEET_JOB, "STORYTELLER", "Find a Storyteller among them (or nearby biomes)."),
                        new AdventureStep(AdventureStepType.OPEN_JOURNAL, 1, "Record them in your journal.")
                ),
                10,
                "Spore-light dreams stick to your boots."));

        register(new AdventureDef(
                "cherry_picnic",
                "Cherry Picnic",
                "Pink petals, baked goods, and a slow afternoon.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "CHERRY", "Meet a Cherry Grove villager."),
                        new AdventureStep(AdventureStepType.MEET_JOB, "BAKER", "Meet a Baker (Cherry folk often bake)."),
                        new AdventureStep(AdventureStepType.GIFT_NPC, 1, "Share a picnic gift.")
                ),
                10,
                "Petals settle on your shoulders like confetti."));

        register(new AdventureDef(
                "badlands_dig",
                "Badlands Dig",
                "Dust, terracotta, and patient archaeology.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "BADLANDS", "Meet a Badlands neighbor."),
                        new AdventureStep(AdventureStepType.MEET_JOB, "ARCHAEOLOGIST", "Meet an Archaeologist."),
                        new AdventureStep(AdventureStepType.COMPLETE_ERRANDS, 1, "Fetch something for their dig table.")
                ),
                12,
                "You leave a careful footprint in the strata."));

        register(new AdventureDef(
                "mountain_beacon",
                "Mountain Beacon",
                "High air, warm wool, and a glowing friend on the ridge.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "MOUNTAIN", "Meet a Mountain villager."),
                        new AdventureStep(AdventureStepType.AWAKEN_PET, 1, "Bring a stuffed companion along."),
                        new AdventureStep(AdventureStepType.PET_GLOW, 1, "Light the way with pet glow.")
                ),
                10,
                "A soft beacon answers across the peaks."));

        register(new AdventureDef(
                "darkwood_lantern",
                "Darkwood Lantern",
                "Deep canopy, careful stories, and a lantern that never scolds.",
                List.of(
                        new AdventureStep(AdventureStepType.MEET_VARIANT, "DARK", "Meet a Dark Forest villager."),
                        new AdventureStep(AdventureStepType.PET_GLOW, 1, "Use pet glow under the canopy."),
                        new AdventureStep(AdventureStepType.MEET_JOB, "STORYTELLER", "Listen to a Storyteller's lantern tale.")
                ),
                12,
                "The dark feels companionable, not empty."));
    }

    private AdventureCatalog() {
    }

    private static void register(AdventureDef def) {
        BY_ID.put(def.id(), def);
    }

    public static AdventureDef get(String id) {
        return BY_ID.get(id);
    }

    public static List<AdventureDef> all() {
        return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
    }

    public static int size() {
        return BY_ID.size();
    }
}
