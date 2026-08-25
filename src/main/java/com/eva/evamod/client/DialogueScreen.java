package com.eva.evamod.client;

import com.eva.evamod.friendship.Hearts;
import com.eva.evamod.net.DialogueActionPayload;
import com.eva.evamod.net.OpenDialoguePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Soft dialogue panel with a scalable action grid (Talk/Trade/Help + personality verbs).
 */
public class DialogueScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 196;
    private static final int TEXT_TOP = 48;
    private static final int TEXT_BOTTOM_PAD = 58;
    private static final int LINE_HEIGHT = 9;

    private final int entityId;
    private String npcName;
    private String jobTitle;
    private String line;
    private int mood;
    private int hearts;
    private String birthday;
    private boolean canErrand;

    public DialogueScreen(OpenDialoguePayload payload) {
        super(Component.literal(payload.npcName()));
        this.entityId = payload.entityId();
        applyPayload(payload);
    }

    public int getEntityId() {
        return entityId;
    }

    public void updateFrom(OpenDialoguePayload payload) {
        applyPayload(payload);
        this.clearWidgets();
        this.init();
    }

    private void applyPayload(OpenDialoguePayload payload) {
        this.npcName = payload.npcName();
        this.jobTitle = payload.jobTitle();
        this.line = payload.line();
        this.mood = payload.mood();
        this.hearts = payload.hearts();
        this.birthday = payload.birthday();
        this.canErrand = payload.canErrand();
    }

    @Override
    protected void init() {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        int row1 = panelTop + PANEL_HEIGHT - 50;
        int row2 = panelTop + PANEL_HEIGHT - 26;
        int buttonWidth = 72;
        int gap = (PANEL_WIDTH - buttonWidth * 4) / 5;

        addAction(panelLeft + gap, row1, buttonWidth, "Talk", DialogueActionPayload.ACTION_TALK);
        addAction(panelLeft + gap * 2 + buttonWidth, row1, buttonWidth, "Trade", DialogueActionPayload.ACTION_TRADE);
        addAction(panelLeft + gap * 3 + buttonWidth * 2, row1, buttonWidth,
                canErrand ? "Help" : "Tip",
                canErrand ? DialogueActionPayload.ACTION_ERRAND : DialogueActionPayload.ACTION_TIP);
        this.addRenderableWidget(Button.builder(Component.literal("Bye"), button -> this.onClose())
                .bounds(panelLeft + gap * 4 + buttonWidth * 3, row1, buttonWidth, 20).build());

        addAction(panelLeft + gap, row2, buttonWidth, "Ask Day", DialogueActionPayload.ACTION_ASK_DAY);
        addAction(panelLeft + gap * 2 + buttonWidth, row2, buttonWidth, "Compliment",
                DialogueActionPayload.ACTION_COMPLIMENT);
        addAction(panelLeft + gap * 3 + buttonWidth * 2, row2, buttonWidth, "Tour",
                DialogueActionPayload.ACTION_TOUR);
        addAction(panelLeft + gap * 4 + buttonWidth * 3, row2, buttonWidth, "Story",
                DialogueActionPayload.ACTION_STORY);
    }

    private void addAction(int x, int y, int w, String label, int action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), button ->
                        ClientPacketDistributor.sendToServer(new DialogueActionPayload(entityId, action)))
                .bounds(x, y, w, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;

        UiStyle.drawPanel(graphics, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);

        Component moodLabel = moodLabel();
        int moodWidth = this.font.width(moodLabel);
        int nameMaxWidth = PANEL_WIDTH - 24 - moodWidth - 8;
        String nameDraw = ellipsize(npcName, nameMaxWidth);
        graphics.text(this.font, Component.literal(nameDraw).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                panelLeft + 12, panelTop + 8, ARGB.opaque(0xFFFFFF), true);
        graphics.text(this.font, moodLabel, panelLeft + PANEL_WIDTH - 12 - moodWidth, panelTop + 8,
                ARGB.opaque(0xFFFFFF), false);

        String heartsDraw = Hearts.bar(hearts) + " · " + birthday;
        graphics.text(this.font, Component.literal(ellipsize(heartsDraw, PANEL_WIDTH - 24)),
                panelLeft + 12, panelTop + 20, ARGB.opaque(0xFF8A9A), false);

        String jobDraw = ellipsize(jobTitle, PANEL_WIDTH - 24);
        graphics.text(this.font, Component.literal(jobDraw).withStyle(ChatFormatting.GRAY),
                panelLeft + 12, panelTop + 32, ARGB.opaque(0xC8C8C8), false);

        drawWrappedLine(graphics, "\"" + line + "\"", panelLeft + 12, panelTop + TEXT_TOP,
                PANEL_WIDTH - 24, PANEL_HEIGHT - TEXT_TOP - TEXT_BOTTOM_PAD);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawWrappedLine(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int maxHeight) {
        int maxLines = Math.max(1, maxHeight / LINE_HEIGHT);
        var lines = this.font.split(Component.literal(text), width);
        int count = Math.min(maxLines, lines.size());
        for (int i = 0; i < count; i++) {
            FormattedCharSequence seq = lines.get(i);
            if (i == maxLines - 1 && lines.size() > maxLines) {
                String plain = this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("...")));
                graphics.text(this.font, Component.literal(plain + "..."),
                        x, y + i * LINE_HEIGHT, ARGB.opaque(0xE8E8E8), false);
            } else {
                graphics.text(this.font, seq, x, y + i * LINE_HEIGHT, ARGB.opaque(0xE8E8E8), false);
            }
        }
    }

    private String ellipsize(String value, int maxWidth) {
        if (value == null) {
            return "";
        }
        if (this.font.width(value) <= maxWidth) {
            return value;
        }
        return this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width("..."))) + "...";
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
