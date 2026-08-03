package com.eva.evamod.client;

import com.eva.evamod.net.OpenTradePayload;
import net.minecraft.client.Minecraft;

public final class ClientTradeHandler {

    public static void handle(OpenTradePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new TradeScreen(payload));
    }

    private ClientTradeHandler() {
    }
}
