package com.eva.evamod.client;

import com.eva.evamod.net.DialogueActionPayload;
import com.eva.evamod.net.OpenDialoguePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Soft dialogue panel. Text colors must be ARGB (full alpha) on 26.2+.
 */
public class DialogueScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 150;

    private final int entityId;
    private String npcName;
    private String jobTitle;
    private String line;
    private int mood;

    public DialogueScreen(OpenDialoguePayload payload) {
        super(Component.literal(payload.npcName()));
        this.entityId = payload.entityId();
        this.npcName = payload.npcName();
        this.jobTitle = payload.jobTitle();
        this.line = payload.line();
        this.mood = payload.mood();
    }

    public int getEntityId() {
        return entityId;
    }

    public void updateFrom(OpenDialoguePayload payload) {
        this.npcName = payload.npcName();
        this.jobTitle = payload.jobTitle();
        this.line = payload.line();
        this.mood = payload.mood();
    }

    @Override
    protected void init() {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - 28;
        int buttonWidth = 84;
        int gap = (PANEL_WIDTH - buttonWidth * 3) / 4;

        this.addRenderableWidget(Button.builder(Component.literal("Talk"), button ->
                        ClientPacketDistributor.sendToServer(new DialogueActionPayload(entityId, DialogueActionPayload.ACTION_TALK)))
                .bounds(panelLeft + gap, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Trade"), button -> {
                    ClientPacketDistributor.sendToServer(new DialogueActionPayload(entityId, DialogueActionPayload.ACTION_TRADE));
                    this.onClose();
                })
                .bounds(panelLeft + gap * 2 + buttonWidth, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Farewell"), button -> this.onClose())
                .bounds(panelLeft + gap * 3 + buttonWidth * 2, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        UiStyle.drawPanel(graphics, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);

        graphics.text(this.font, Component.literal(npcName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                panelLeft + 12, panelTop + 10, ARGB.opaque(0xFFFFFF), true);
        graphics.text(this.font, Component.literal(jobTitle).withStyle(ChatFormatting.GRAY),
                panelLeft + 12, panelTop + 22, ARGB.opaque(0xC8C8C8), false);

        Component moodLabel = moodLabel();
        int moodWidth = this.font.width(moodLabel);
        graphics.text(this.font, moodLabel, panelLeft + PANEL_WIDTH - 12 - moodWidth, panelTop + 10,
                ARGB.opaque(0xFFFFFF), false);

        graphics.textWithWordWrap(this.font, Component.literal("\"" + line + "\""),
                panelLeft + 12, panelTop + 42, PANEL_WIDTH - 24, ARGB.opaque(0xE8E8E8), false);
    }

    private Component moodLabel() {
        return switch (mood) {
            case 0 -> Component.literal("Resentful").withStyle(ChatFormatting.RED);
            case 1 -> Component.literal("Annoyed").withStyle(ChatFormatting.GOLD);
            case 3 -> Component.literal("Friendly").withStyle(ChatFormatting.GREEN);
            case 4 -> Component.literal("Close Friend").withStyle(ChatFormatting.AQUA);
            default -> Component.literal("Neutral").withStyle(ChatFormatting.WHITE);
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
