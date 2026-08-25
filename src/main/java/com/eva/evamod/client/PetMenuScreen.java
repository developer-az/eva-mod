package com.eva.evamod.client;

import com.eva.evamod.net.OpenPetMenuPayload;
import com.eva.evamod.net.PetMenuActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Dialogue-like utility menu for stuffed-animal pets. */
public class PetMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 168;
    private static final int LINE_HEIGHT = 9;

    private final int entityId;
    private String petName;
    private String kindTitle;
    private String line;
    private boolean sitting;
    private boolean glowing;
    private boolean hasTrinket;

    public PetMenuScreen(OpenPetMenuPayload payload) {
        super(Component.literal(payload.petName()));
        this.entityId = payload.entityId();
        apply(payload);
    }

    public int getEntityId() {
        return entityId;
    }

    public void updateFrom(OpenPetMenuPayload payload) {
        apply(payload);
        this.clearWidgets();
        this.init();
    }

    private void apply(OpenPetMenuPayload payload) {
        this.petName = payload.petName();
        this.kindTitle = payload.kindTitle();
        this.line = payload.line();
        this.sitting = payload.sitting();
        this.glowing = payload.glowing();
        this.hasTrinket = payload.hasTrinket();
    }

    @Override
    protected void init() {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        int row1 = panelTop + PANEL_HEIGHT - 50;
        int row2 = panelTop + PANEL_HEIGHT - 26;
        int w = 88;
        int gap = (PANEL_WIDTH - w * 3) / 4;

        add(panelLeft + gap, row1, w, sitting ? "Follow" : "Sit", PetMenuActionPayload.ACTION_SIT_FOLLOW);
        add(panelLeft + gap * 2 + w, row1, w, glowing ? "Dim" : "Glow", PetMenuActionPayload.ACTION_GLOW);
        add(panelLeft + gap * 3 + w * 2, row1, w, "Find Town", PetMenuActionPayload.ACTION_FIND);

        add(panelLeft + gap, row2, w, "Cheer", PetMenuActionPayload.ACTION_CHEER);
        add(panelLeft + gap * 2 + w, row2, w, hasTrinket ? "Trinket" : "Carry Tip",
                PetMenuActionPayload.ACTION_TRINKET_HINT);
        this.addRenderableWidget(Button.builder(Component.literal("Bye"), button -> this.onClose())
                .bounds(panelLeft + gap * 3 + w * 2, row2, w, 20).build());
    }

    private void add(int x, int y, int w, String label, int action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), button ->
                        ClientPacketDistributor.sendToServer(new PetMenuActionPayload(entityId, action)))
                .bounds(x, y, w, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        UiStyle.drawPanel(graphics, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);

        graphics.text(this.font, Component.literal(petName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                panelLeft + 12, panelTop + 8, ARGB.opaque(0xFFFFFF), true);
        graphics.text(this.font, Component.literal(kindTitle).withStyle(ChatFormatting.GRAY),
                panelLeft + 12, panelTop + 22, ARGB.opaque(0xC8C8C8), false);

        String status = (sitting ? "Sitting" : "Following")
                + (glowing ? " · Glowing" : "")
                + (hasTrinket ? " · Carrying" : "");
        graphics.text(this.font, Component.literal(status),
                panelLeft + 12, panelTop + 34, ARGB.opaque(0xA0D8FF), false);

        var lines = this.font.split(Component.literal("\"" + line + "\""), PANEL_WIDTH - 24);
        int max = Math.min(4, lines.size());
        for (int i = 0; i < max; i++) {
            FormattedCharSequence seq = lines.get(i);
            graphics.text(this.font, seq, panelLeft + 12, panelTop + 48 + i * LINE_HEIGHT,
                    ARGB.opaque(0xE8E8E8), false);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
