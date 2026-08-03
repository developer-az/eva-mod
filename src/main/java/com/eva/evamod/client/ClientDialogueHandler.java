package com.eva.evamod.client;

import com.eva.evamod.net.OpenDialoguePayload;
import net.minecraft.client.Minecraft;

public final class ClientDialogueHandler {

    public static void handle(OpenDialoguePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof DialogueScreen screen && screen.getEntityId() == payload.entityId()) {
            screen.updateFrom(payload);
        } else {
            minecraft.gui.setScreen(new DialogueScreen(payload));
        }
    }

    private ClientDialogueHandler() {
    }
}
