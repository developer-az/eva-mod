package com.eva.evamod.client;

import com.eva.evamod.net.OpenTradePayload;
import com.eva.evamod.net.TradeActionPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Cute custom trade UI — not the vanilla villager merchant screen. */
public class TradeScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int ROW_HEIGHT = 28;
    private static final int HEADER = 36;
    private static final int FOOTER = 32;

    private final int entityId;
    private final String npcName;
    private final List<OpenTradePayload.TradeRow> rows;
    private int selected = 0;

    public TradeScreen(OpenTradePayload payload) {
        super(Component.literal("Trade with " + payload.npcName()));
        this.entityId = payload.entityId();
        this.npcName = payload.npcName();
        this.rows = new ArrayList<>(payload.rows());
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    protected void init() {
        int panelHeight = HEADER + Math.max(1, rows.size()) * ROW_HEIGHT + FOOTER + 8;
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - panelHeight) / 2;
        int buttonY = panelTop + panelHeight - 26;

        this.addRenderableWidget(Button.builder(Component.literal("Deal"), button -> {
                    if (!rows.isEmpty() && selected >= 0 && selected < rows.size()) {
                        ClientPacketDistributor.sendToServer(new TradeActionPayload(entityId, selected));
                    }
                })
                .bounds(panelLeft + 16, buttonY, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(panelLeft + PANEL_WIDTH - 136, buttonY, 120, 20).build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int panelHeight = HEADER + Math.max(1, rows.size()) * ROW_HEIGHT + FOOTER + 8;
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - panelHeight) / 2;
        for (int i = 0; i < rows.size(); i++) {
            int rowTop = panelTop + HEADER + i * ROW_HEIGHT;
            if (mouseX >= panelLeft + 8 && mouseX <= panelLeft + PANEL_WIDTH - 8
                    && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT - 2) {
                this.selected = i;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelHeight = HEADER + Math.max(1, rows.size()) * ROW_HEIGHT + FOOTER + 8;
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - panelHeight) / 2;

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        UiStyle.drawPanel(graphics, panelLeft, panelTop, PANEL_WIDTH, panelHeight);

        graphics.text(this.font, Component.literal(npcName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                panelLeft + 12, panelTop + 10, ARGB.opaque(0xFFFFFF), true);
        graphics.text(this.font, Component.literal("Little trades").withStyle(ChatFormatting.GRAY),
                panelLeft + 12, panelTop + 22, ARGB.opaque(0xC8C8C8), false);

        if (rows.isEmpty()) {
            graphics.text(this.font, Component.literal("Nothing to trade today."),
                    panelLeft + 12, panelTop + HEADER + 6, ARGB.opaque(0xE8E8E8), false);
        }

        for (int i = 0; i < rows.size(); i++) {
            OpenTradePayload.TradeRow row = rows.get(i);
            int rowTop = panelTop + HEADER + i * ROW_HEIGHT;
            int bg = i == selected ? 0x55C4A484 : 0x22000000;
            graphics.fill(panelLeft + 8, rowTop, panelLeft + PANEL_WIDTH - 8, rowTop + ROW_HEIGHT - 2, bg);

            drawStack(graphics, row.cost(), panelLeft + 14, rowTop + 5);
            graphics.text(this.font, Component.literal("->"),
                    panelLeft + 48, rowTop + 9, ARGB.opaque(0xFFFFFF), false);
            drawStack(graphics, row.result(), panelLeft + 64, rowTop + 5);

            String uses = row.uses() + "/" + row.maxUses();
            boolean soldOut = row.uses() >= row.maxUses();
            Component label = Component.literal(soldOut ? "Sold out" : uses)
                    .withStyle(soldOut ? ChatFormatting.RED : ChatFormatting.GREEN);
            graphics.text(this.font, label, panelLeft + PANEL_WIDTH - 12 - this.font.width(label),
                    rowTop + 9, ARGB.opaque(0xFFFFFF), false);
        }
    }

    private void drawStack(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.item(stack, x, y);
        graphics.itemDecorations(this.font, stack, x, y);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
