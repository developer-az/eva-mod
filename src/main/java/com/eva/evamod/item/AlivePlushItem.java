package com.eva.evamod.item;

import com.eva.evamod.entity.StuffedPet;
import com.eva.evamod.pet.PetKind;
import com.eva.evamod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Right-click a block to awaken a stuffed pet companion. Optional CustomData
 * {@code PetKind} selects the form; otherwise a random kind awakens.
 */
public class AlivePlushItem extends Item {
    public static final String KIND_TAG = "PetKind";

    public AlivePlushItem(Properties properties) {
        super(properties);
    }

    public static ItemStack withKind(ItemStack stack, PetKind kind) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(KIND_TAG, kind.name()));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Alive " + kind.getDisplayName() + " Plush"));
        return stack;
    }

    public static PetKind kindOf(ItemStack stack, net.minecraft.util.RandomSource random) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains(KIND_TAG)) {
            CompoundTag tag = data.copyTag();
            return PetKind.byName(tag.getStringOr(KIND_TAG, PetKind.TEDDY.name()));
        }
        return PetKind.random(random);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clicked);
        BlockPos spawnPos = clickedState.getCollisionShape(level, clicked).isEmpty()
                ? clicked
                : clicked.relative(face);

        PetKind kind = kindOf(stack, serverLevel.getRandom());
        StuffedPet pet = ModEntities.STUFFED_PET.get().spawn(
                serverLevel,
                entity -> {
                    entity.setKind(kind);
                    if (player instanceof ServerPlayer serverPlayer) {
                        entity.awakenFor(serverPlayer, kind);
                    } else {
                        entity.setPersistenceRequired();
                        entity.setCustomName(Component.literal(kind.randomName(serverLevel.getRandom())));
                        entity.setCustomNameVisible(true);
                    }
                },
                spawnPos,
                EntitySpawnReason.SPAWN_ITEM_USE,
                true,
                !clicked.equals(spawnPos) && face == Direction.UP);

        if (pet == null) {
            return InteractionResult.FAIL;
        }

        serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
        serverLevel.playSound(null, spawnPos, SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.NEUTRAL, 0.8F, 1.2F);
        stack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
