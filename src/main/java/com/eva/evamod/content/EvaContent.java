package com.eva.evamod.content;

import com.eva.evamod.quest.Errand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;

/**
 * Extension points for future content without breaking saves.
 * <p>
 * Add new dialogue packs, gift extras, or errand items here (or from another mod on the
 * same classpath) instead of hard-forking core classes. Keep registrations static/init-time
 * — never allocate per tick.
 */
public final class EvaContent {
    private static final List<Item> EXTRA_ERRAND_ITEMS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<StringBuilder>> GUIDE_PAGE_HOOKS = new CopyOnWriteArrayList<>();

    private EvaContent() {
    }

    /** Optional errand pool extras for future seasons / addons. */
    public static void registerErrandItem(Item item) {
        if (item != null) {
            EXTRA_ERRAND_ITEMS.add(item);
        }
    }

    public static List<Item> extraErrandItems() {
        return Collections.unmodifiableList(EXTRA_ERRAND_ITEMS);
    }

    /** Optional extra guide-book blurb lines for future features. */
    public static void registerGuideNote(Consumer<StringBuilder> note) {
        if (note != null) {
            GUIDE_PAGE_HOOKS.add(note);
        }
    }

    public static List<String> guideAddonNotes() {
        List<String> notes = new ArrayList<>();
        for (Consumer<StringBuilder> hook : GUIDE_PAGE_HOOKS) {
            StringBuilder sb = new StringBuilder();
            hook.accept(sb);
            if (!sb.isEmpty()) {
                notes.add(sb.toString());
            }
        }
        return notes;
    }

    /** Reserved for future errand generation hooks (unused by default). */
    public static void onErrandCreated(Errand errand) {
        // Intentionally empty — addons can wrap Errand.create later.
    }
}
