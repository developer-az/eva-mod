package com.eva.evamod.client;

import com.eva.evamod.net.OpenTradePayload;
import net.minecraft.client.Minecraft;

public final class ClientTradeHandler {

    public static void handle(OpenTradePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof TradeScreen screen && screen.getEntityId() == payload.entityId()) {
            screen.updateFrom(payload);
        } else {
            minecraft.gui.setScreen(new TradeScreen(payload));
        }
    }

    private ClientTradeHandler() {
    }
}