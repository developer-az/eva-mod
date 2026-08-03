package com.eva.evamod.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared soft dark-panel look for dialogue and trade screens. */
public final class UiStyle {
    public static final int PANEL_FILL = 0xD0181420;
    public static final int PANEL_BORDER = 0xFFC4A484;
    public static final int PANEL_INNER = 0xFF8B7355;

    public static void drawPanel(GuiGraphicsExtractor graphics, int left, int top, int width, int height) {
        graphics.fill(left, top, left + width, top + height, PANEL_FILL);
        graphics.fill(left, top, left + width, top + 1, PANEL_BORDER);
        graphics.fill(left, top + height - 1, left + width, top + height, PANEL_BORDER);
        graphics.fill(left, top, left + 1, top + height, PANEL_BORDER);
        graphics.fill(left + width - 1, top, left + width, top + height, PANEL_BORDER);
        graphics.fill(left + 2, top + 2, left + width - 2, top + 3, PANEL_INNER);
    }

    private UiStyle() {
    }
}
