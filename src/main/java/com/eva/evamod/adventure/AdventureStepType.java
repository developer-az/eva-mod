package com.eva.evamod.adventure;

/**
 * What a single adventure step asks the player to do.
 * Steps are evaluated server-side from existing gameplay hooks — no combat.
 */
public enum AdventureStepType {
    /** Meet any number of Biome NPCs (house index size). */
    MEET_NPCS,
    /** Meet an NPC of a specific biome variant (variant name, e.g. OCEAN). */
    MEET_VARIANT,
    /** Meet an NPC with a specific job (job name, e.g. STORYTELLER). */
    MEET_JOB,
    /** Complete N help-wanted errands. */
    COMPLETE_ERRANDS,
    /** Awaken at least one stuffed pet. */
    AWAKEN_PET,
    /** Use pet glow utility once. */
    PET_GLOW,
    /** Use pet find-settlement utility once. */
    PET_FIND,
    /** Run locate / town successfully N times (tracked counter). */
    LOCATE_SETTLEMENTS,
    /** Discover a town (npc_town) via locate/town. */
    DISCOVER_TOWN,
    /** Open journal command once. */
    OPEN_JOURNAL,
    /** Read mail at least once. */
    READ_MAIL,
    /** Gift any NPC once. */
    GIFT_NPC,
    /** Check the season calendar once. */
    CHECK_CALENDAR
}
