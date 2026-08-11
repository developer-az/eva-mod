package com.eva.evamod.adventure;

import com.eva.evamod.content.EvaContent;

/** Registers adventure guide-book notes. */
public final class AdventureBootstrap {
    private AdventureBootstrap() {
    }

    public static void register() {
        EvaContent.registerGuideNote(sb -> sb
                .append("Adventures\n\n")
                .append(AdventureCatalog.size())
                .append(" cozy multi-step stories unlock as you explore.\n\n")
                .append("/evamod adventure — list progress\n")
                .append("/evamod adventure start <id> — begin a story\n\n")
                .append("Starter arcs: first_steps, soft_companion.\n")
                .append("Later: lost_stitches, cartographers_trail, biome tales…"));
    }
}
