package com.eva.evamod.trade;

import com.eva.evamod.entity.NpcJob;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

/**
 * Builds the daily trade stock for an NPC. Offers are picked deterministically
 * from the job's pool using (world day, npc uuid) as seed, so stock rotates
 * every Minecraft morning but stays stable within a day.
 */
public final class NpcTrades {
    /** cost -> result, both fixed counts. maxUses limits how often per day. */
    private record TradeTemplate(ItemLike cost, int costCount, ItemLike result, int resultCount, int maxUses) {
        MerchantOffer toOffer() {
            return new MerchantOffer(new ItemCost(cost.asItem(), costCount),
                    new ItemStack(result.asItem(), resultCount), maxUses, 2, 0.05F);
        }
    }

    private static final Map<NpcJob, List<TradeTemplate>> POOLS = new EnumMap<>(NpcJob.class);
    private static final int OFFERS_PER_DAY = 5;

    private static void pool(NpcJob job, TradeTemplate... templates) {
        POOLS.put(job, List.of(templates));
    }

    static {
        pool(NpcJob.FARMER,
                new TradeTemplate(Items.WHEAT, 20, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.CARROT, 22, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.POTATO, 26, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.PUMPKIN, 6, Items.EMERALD, 1, 8),
                new TradeTemplate(Items.EMERALD, 1, Items.BREAD, 6, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.APPLE, 4, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.CAKE, 1, 4),
                new TradeTemplate(Items.EMERALD, 5, Items.GOLDEN_CARROT, 3, 6));
        pool(NpcJob.SHEPHERD,
                new TradeTemplate(Items.WOOL.pick(DyeColor.WHITE), 18, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.SHEARS, 1, 4),
                new TradeTemplate(Items.EMERALD, 1, Items.WOOL.pick(DyeColor.WHITE), 8, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.PAINTING, 3, 8),
                new TradeTemplate(Items.EMERALD, 1, Items.COOKED_MUTTON, 6, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.BED.pick(DyeColor.WHITE), 1, 4));
        pool(NpcJob.FISHERMAN,
                new TradeTemplate(Items.STRING, 20, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.COD, 15, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.COOKED_COD, 6, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.COOKED_SALMON, 6, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.FISHING_ROD, 1, 4),
                new TradeTemplate(Items.EMERALD, 2, Items.CAMPFIRE, 1, 6));
        pool(NpcJob.LUMBERJACK,
                new TradeTemplate(Items.SPRUCE_LOG, 12, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.STICK, 32, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.CHARCOAL, 8, 12),
                new TradeTemplate(Items.EMERALD, 4, Items.IRON_AXE, 1, 3),
                new TradeTemplate(Items.EMERALD, 2, Items.BARREL, 1, 8),
                new TradeTemplate(Items.EMERALD, 2, Items.CAMPFIRE, 1, 6));
        pool(NpcJob.TRAPPER,
                new TradeTemplate(Items.RABBIT_HIDE, 9, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.LEATHER, 6, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 4, Items.LEATHER_BOOTS, 1, 4),
                new TradeTemplate(Items.EMERALD, 2, Items.RABBIT_STEW, 1, 8),
                new TradeTemplate(Items.EMERALD, 1, Items.COOKED_RABBIT, 4, 12),
                new TradeTemplate(Items.EMERALD, 8, Items.SADDLE, 1, 2));
        pool(NpcJob.HERBALIST,
                new TradeTemplate(Items.SWEET_BERRIES, 10, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.DANDELION, 12, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.SUGAR, 8, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.HONEY_BOTTLE, 1, 6),
                new TradeTemplate(Items.EMERALD, 2, Items.GLOW_BERRIES, 4, 8),
                new TradeTemplate(Items.EMERALD, 1, Items.MELON_SLICE, 8, 12));
        pool(NpcJob.MINER,
                new TradeTemplate(Items.RAW_IRON, 5, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.RAW_COPPER, 16, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.COAL, 12, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.TORCH, 16, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.LANTERN, 1, 8),
                new TradeTemplate(Items.EMERALD, 5, Items.IRON_PICKAXE, 1, 3));
        pool(NpcJob.CARTOGRAPHER,
                new TradeTemplate(Items.PAPER, 24, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.MAP, 1, 8),
                new TradeTemplate(Items.EMERALD, 4, Items.COMPASS, 1, 4),
                new TradeTemplate(Items.EMERALD, 5, Items.SPYGLASS, 1, 3),
                new TradeTemplate(Items.EMERALD, 1, Items.PAPER, 12, 12));
        pool(NpcJob.GLASSBLOWER,
                new TradeTemplate(Items.SAND, 32, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.AMETHYST_SHARD, 4, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.GLASS, 8, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.GLASS_BOTTLE, 6, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.TINTED_GLASS, 2, 8));
        pool(NpcJob.NOMAD,
                new TradeTemplate(Items.LEATHER, 8, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.BONE, 16, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.LEAD, 1, 8),
                new TradeTemplate(Items.EMERALD, 2, Items.PAINTING, 1, 8),
                new TradeTemplate(Items.EMERALD, 2, Items.CAMPFIRE, 1, 6),
                new TradeTemplate(Items.EMERALD, 8, Items.NAME_TAG, 1, 2));
        pool(NpcJob.BAKER,
                new TradeTemplate(Items.WHEAT, 20, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.SUGAR, 16, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.BREAD, 8, 12),
                new TradeTemplate(Items.EMERALD, 1, Items.COOKIE, 10, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.CAKE, 1, 4),
                new TradeTemplate(Items.EMERALD, 2, Items.PUMPKIN_PIE, 1, 8));
        pool(NpcJob.BEEKEEPER,
                new TradeTemplate(Items.HONEYCOMB, 4, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.POPPY, 12, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.HONEY_BOTTLE, 2, 8),
                new TradeTemplate(Items.EMERALD, 3, Items.HONEYCOMB, 3, 6),
                new TradeTemplate(Items.EMERALD, 4, Items.BEEHIVE, 1, 3),
                new TradeTemplate(Items.EMERALD, 1, Items.HONEY_BLOCK, 1, 6));
        pool(NpcJob.STORYTELLER,
                new TradeTemplate(Items.PAPER, 24, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.BOOK, 4, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 2, Items.BOOK, 2, 8),
                new TradeTemplate(Items.EMERALD, 3, Items.WRITABLE_BOOK, 1, 6),
                new TradeTemplate(Items.EMERALD, 1, Items.INK_SAC, 4, 12),
                new TradeTemplate(Items.EMERALD, 5, Items.LECTERN, 1, 3));
        pool(NpcJob.ARCHAEOLOGIST,
                new TradeTemplate(Items.BRUSH, 1, Items.EMERALD, 2, 6),
                new TradeTemplate(Items.SAND, 32, Items.EMERALD, 1, 12),
                new TradeTemplate(Items.EMERALD, 3, Items.BRUSH, 1, 4),
                new TradeTemplate(Items.EMERALD, 2, Items.TORCH, 16, 12),
                new TradeTemplate(Items.EMERALD, 4, Items.DECORATED_POT, 1, 4),
                new TradeTemplate(Items.EMERALD, 2, Items.SUSPICIOUS_SAND, 2, 6));
    }

    public static MerchantOffers buildDailyOffers(NpcJob job, long day, UUID npcId) {
        RandomSource random = RandomSource.create(day * 341873128712L + npcId.getLeastSignificantBits());
        List<TradeTemplate> pool = new ArrayList<>(POOLS.get(job));
        MerchantOffers offers = new MerchantOffers();
        int count = Math.min(OFFERS_PER_DAY, pool.size());
        for (int i = 0; i < count; i++) {
            TradeTemplate template = pool.remove(random.nextInt(pool.size()));
            offers.add(template.toOffer());
        }
        return offers;
    }

    /** Bonus offer only good friends get to see. */
    public static MerchantOffer friendOffer(NpcJob job) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, 1),
                new ItemStack(Items.DIAMOND, 1), 1, 5, 0.05F);
    }

    private NpcTrades() {
    }
}
