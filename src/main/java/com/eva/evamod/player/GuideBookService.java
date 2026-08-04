package com.eva.evamod.player;

import com.eva.evamod.ModVersions;
import com.eva.evamod.content.EvaContent;
import com.eva.evamod.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Homestead Primer — written book with clickable commands.
 * Delivery is once-per-player (persisted), O(1) on login, MP-safe.
 */
public final class GuideBookService {
    public static final String TITLE = "Homestead Primer";
    public static final String AUTHOR = "Eva Mod";

    private GuideBookService() {
    }

    public static boolean isGuideBook(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.WRITTEN_BOOK)) {
            return false;
        }
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            return false;
        }
        return AUTHOR.equals(content.author()) && TITLE.equals(content.title().raw());
    }

    public static ItemStack create() {
        List<Filterable<Component>> pages = new ArrayList<>();
        pages.add(page(
                Component.literal("Eva Mod " + ModVersions.DISPLAY + "\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal(ModVersions.CODENAME + "\n\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("Tap a blue line to suggest the command in chat.\n\n").withStyle(ChatFormatting.DARK_GRAY))
                        .append(cmd("/evamod", "Full help"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod town", "Find nearest town"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod locate", "Find next house"))));
        pages.add(page(
                Component.literal("Getting around\n").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD)
                        .append(cmd("/evamod near", "NPCs near you"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod journal", "Friends & birthdays"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod mail", "Letters"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod calendar", "Season / festival"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod errand", "Active errand"))
                        .append(Component.literal("\n"))
                        .append(cmd("/evamod book", "Get another primer"))));
        pages.add(page(
                Component.literal("Old worlds\n").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                        .append(Component.literal(
                                        "Chunks explored before Eva Mod never grew houses. Travel to new land, or plant one starter home:\n\n")
                                .withStyle(ChatFormatting.BLACK))
                        .append(cmd("/evamod settle", "Founder's Homestead (once per world)"))
                        .append(Component.literal("\n\nCheap & safe: no chunk regen, one cottage + neighbor.")
                                .withStyle(ChatFormatting.DARK_GRAY))));
        pages.add(page(
                Component.literal("Friendship\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        .append(Component.literal(
                                        "Talk, trade, gift. Hearts unlock Help errands & heart events. Birthdays & festivals matter.\n\nSneak + right-click with an item to gift.\n\n")
                                .withStyle(ChatFormatting.BLACK))
                        .append(cmd("/evamod version", "Show version"))));

        for (String note : EvaContent.guideAddonNotes()) {
            if (note != null && !note.isBlank()) {
                pages.add(page(Component.literal(note).withStyle(ChatFormatting.BLACK)));
            }
        }

        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough(TITLE),
                AUTHOR,
                0,
                pages,
                true);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        book.set(DataComponents.CUSTOM_NAME, Component.literal(TITLE).withStyle(ChatFormatting.GOLD));
        return book;
    }

    /**
     * Give the primer at most once per player forever (persisted flag).
     * If they already hold one, just mark the flag — no dupes, no lag.
     */
    public static boolean tryGiveOnce(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.receivedGuideBook()) {
            return false;
        }
        if (playerHasGuideBook(player)) {
            data.setReceivedGuideBook(true);
            player.setData(ModAttachments.PLAYER_DATA, data.copy());
            return false;
        }

        // Mark first so a crash mid-give cannot infinitely re-drop on every login.
        data.setReceivedGuideBook(true);
        player.setData(ModAttachments.PLAYER_DATA, data.copy());

        ItemStack book = create();
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        player.sendSystemMessage(Component.literal("You received the Homestead Primer (written book). Open it for commands.")
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    /** Always give a fresh copy (does not reset the once-flag). */
    public static void giveAnother(ServerPlayer player) {
        ItemStack book = create();
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    private static boolean playerHasGuideBook(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (isGuideBook(player.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static Filterable<Component> page(Component component) {
        return Filterable.passThrough(component);
    }

    private static MutableComponent cmd(String command, String hover) {
        return Component.literal(command).withStyle(Style.EMPTY
                .withColor(ChatFormatting.BLUE)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
    }
}
