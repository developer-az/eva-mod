package com.eva.evamod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * A simple fetch errand from an NPC — bring them an item for friendship reward.
 * Keeps adventure loops short and personal (Stardew "help wanted" vibe).
 */
public record Errand(
        String npcName,
        String npcId,
        String itemId,
        int count,
        int rewardRep,
        long issuedDay,
        boolean completed) {

    public static final Codec<Errand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("npcName").forGetter(Errand::npcName),
            Codec.STRING.optionalFieldOf("npcId", "").forGetter(Errand::npcId),
            Codec.STRING.fieldOf("itemId").forGetter(Errand::itemId),
            Codec.INT.fieldOf("count").forGetter(Errand::count),
            Codec.INT.fieldOf("rewardRep").forGetter(Errand::rewardRep),
            Codec.LONG.fieldOf("issuedDay").forGetter(Errand::issuedDay),
            Codec.BOOL.fieldOf("completed").forGetter(Errand::completed)
    ).apply(instance, Errand::new));

    public static final Codec<List<Errand>> LIST_CODEC = CODEC.listOf();

    private static final Item[] ERRAND_POOL = {
            Items.WHEAT, Items.CARROT, Items.POTATO, Items.APPLE, Items.SWEET_BERRIES,
            Items.COAL, Items.IRON_NUGGET, Items.STRING, Items.FEATHER, Items.LEATHER,
            Items.OAK_LOG, Items.WOOL.pick(DyeColor.WHITE), Items.CLAY_BALL, Items.PAPER, Items.GLASS_BOTTLE,
            Items.COD, Items.BONE, Items.INK_SAC, Items.HONEYCOMB, Items.PUMPKIN
    };

    public static Errand create(String npcName, UUID npcUuid, long day, RandomSource random) {
        Item item = ERRAND_POOL[random.nextInt(ERRAND_POOL.length)];
        int count = 1 + random.nextInt(4);
        int reward = 8 + random.nextInt(8);
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return new Errand(npcName, npcUuid.toString(), id.toString(), count, reward, day, false);
    }

    public Item item() {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return Items.AIR;
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == null ? Items.AIR : item;
    }

    public String itemDisplayName() {
        Item item = item();
        if (item == Items.AIR) {
            return itemId;
        }
        return new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
    }

    public Errand markCompleted() {
        return new Errand(npcName, npcId, itemId, count, rewardRep, issuedDay, true);
    }

    public String describe() {
        return "Bring " + count + "× " + itemDisplayName() + " to " + npcName;
    }

    public boolean matchesNpc(UUID uuid) {
        return npcId != null && !npcId.isEmpty() && npcId.equals(uuid.toString());
    }
}
