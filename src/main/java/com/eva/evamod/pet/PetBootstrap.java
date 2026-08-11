package com.eva.evamod.pet;

import com.eva.evamod.content.EvaContent;

/** Registers pet-related guide-book notes and future content hooks. */
public final class PetBootstrap {
    private PetBootstrap() {
    }

    public static void register() {
        EvaContent.registerGuideNote(sb -> sb
                .append("Stuffed Pets\n\n")
                .append("Place an Alive Plush on the ground to awaken a cozy companion.\n\n")
                .append("Empty hand: sit / follow\n")
                .append("Dye: ribbon color\n")
                .append("Treats: cookies, berries, honey\n")
                .append("Glow Berry: soft glow\n")
                .append("Sneak + item: carry one trinket\n\n")
                .append("/evamod pet — status, sit, follow, glow, find, here"));
    }
}
