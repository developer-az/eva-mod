package com.eva.evamod.gift;

import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.NpcPersonality;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Stardew-style gift tastes. Every NPC has one personal LOVE gift; jobs and
 * personalities add likes/dislikes so gift-giving stays meaningful.
 */
public enum GiftTaste {
    LOVE(15, "loved"),
    LIKE(8, "liked"),
    NEUTRAL(3, "accepted"),
    DISLIKE(-4, "disliked"),
    HATE(-10, "hated");

    private final int reputationDelta;
    private final String verb;

    GiftTaste(int reputationDelta, String verb) {
        this.reputationDelta = reputationDelta;
        this.verb = verb;
    }

    public int reputationDelta() {
        return reputationDelta;
    }

    public String verb() {
        return verb;
    }

    private static final Item[] PERSONAL_LOVES = {
            Items.COOKIE, Items.PUMPKIN_PIE, Items.APPLE, Items.SWEET_BERRIES,
            Items.HONEY_BOTTLE, Items.GLOW_BERRIES, Items.CAKE, Items.POPPY,
            Items.SUNFLOWER, Items.MELON_SLICE, Items.BAKED_POTATO, Items.COCOA_BEANS,
            Items.GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.PINK_PETALS, Items.LILAC
    };

    private static final Set<Item> UNIVERSAL_HATE = Set.of(
            Items.ROTTEN_FLESH, Items.POISONOUS_POTATO, Items.SPIDER_EYE,
            Items.PUFFERFISH, Items.WITHER_ROSE);

    private static final Set<Item> UNIVERSAL_DISLIKE = Set.of(
            Items.DIRT, Items.GRAVEL, Items.COBBLESTONE, Items.STICK, Items.BONE);

    public static Item personalLove(UUID npcId) {
        return PERSONAL_LOVES[Math.floorMod(npcId.hashCode(), PERSONAL_LOVES.length)];
    }

    public static GiftTaste rate(Item item, UUID npcId, NpcJob job, NpcPersonality personality) {
        if (item == personalLove(npcId)) {
            return LOVE;
        }
        if (UNIVERSAL_HATE.contains(item)) {
            return HATE;
        }
        if (UNIVERSAL_DISLIKE.contains(item)) {
            return DISLIKE;
        }
        if (jobLoves(job, item) || personalityLoves(personality, item)) {
            return LIKE;
        }
        if (jobDislikes(job, item)) {
            return DISLIKE;
        }
        if (new net.minecraft.world.item.ItemStack(item).get(net.minecraft.core.component.DataComponents.FOOD) != null) {
            return LIKE;
        }
        return NEUTRAL;
    }

    private static boolean jobLoves(NpcJob job, Item item) {
        return switch (job) {
            case FARMER -> item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO
                    || item == Items.BEETROOT || item == Items.BONE_MEAL;
            case SHEPHERD -> item == Items.WOOL.pick(DyeColor.WHITE) || item == Items.SHEARS || item == Items.WHEAT;
            case FISHERMAN -> item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH
                    || item == Items.FISHING_ROD;
            case LUMBERJACK -> item == Items.OAK_LOG || item == Items.APPLE || item == Items.HONEY_BOTTLE;
            case TRAPPER -> item == Items.RABBIT || item == Items.LEATHER || item == Items.COOKED_RABBIT;
            case HERBALIST -> item == Items.POPPY || item == Items.DANDELION || item == Items.LILY_OF_THE_VALLEY
                    || item == Items.GLOW_BERRIES;
            case MINER -> item == Items.IRON_INGOT || item == Items.COAL || item == Items.TORCH
                    || item == Items.RAW_GOLD;
            case CARTOGRAPHER -> item == Items.PAPER || item == Items.MAP || item == Items.COMPASS
                    || item == Items.INK_SAC;
            case GLASSBLOWER -> item == Items.GLASS || item == Items.SAND || item == Items.GLASS_BOTTLE;
            case NOMAD -> item == Items.BREAD || item == Items.LEATHER || item == Items.ENDER_PEARL;
        };
    }

    private static boolean jobDislikes(NpcJob job, Item item) {
        return switch (job) {
            case FISHERMAN -> item == Items.COOKED_COD || item == Items.COOKED_SALMON; // prefers fresh
            case HERBALIST -> item == Items.FERMENTED_SPIDER_EYE;
            case MINER -> item == Items.DIRT;
            default -> false;
        };
    }

    private static boolean personalityLoves(NpcPersonality personality, Item item) {
        return switch (personality) {
            case CHEERFUL -> item == Items.COOKIE || item == Items.CAKE || item == Items.SUNFLOWER;
            case SHY -> item == Items.BOOK || item == Items.SWEET_BERRIES || item == Items.POPPY;
            case DRAMATIC -> item == Items.GOLDEN_APPLE || item == Items.DYE.pick(DyeColor.RED) || item == Items.AMETHYST_SHARD;
            case GRUMPY -> item == Items.BAKED_POTATO || item == Items.COOKED_BEEF || item == Items.COAL;
            case SLEEPY -> item == Items.HONEY_BOTTLE || item == Items.MILK_BUCKET || item == Items.WOOL.pick(DyeColor.WHITE);
            case CURIOUS -> item == Items.ENDER_PEARL || item == Items.SPYGLASS || item == Items.BRUSH;
        };
    }
}
