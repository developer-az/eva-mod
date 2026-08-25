package com.eva.evamod.client;

import com.eva.evamod.net.OpenPetMenuPayload;
import net.minecraft.client.Minecraft;

public final class ClientPetMenuHandler {

    public static void handle(OpenPetMenuPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof PetMenuScreen screen && screen.getEntityId() == payload.entityId()) {
            screen.updateFrom(payload);
        } else {
            minecraft.gui.setScreen(new PetMenuScreen(payload));
        }
    }

    private ClientPetMenuHandler() {
    }
}
