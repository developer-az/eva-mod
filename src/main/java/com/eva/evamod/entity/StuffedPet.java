package com.eva.evamod.entity;

import com.eva.evamod.command.EvaModCommands;
import com.eva.evamod.command.SettlementLocator;
import com.eva.evamod.entity.ai.PetFollowOwnerGoal;
import com.eva.evamod.entity.ai.PetSitGoal;
import com.eva.evamod.pet.PetKind;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.world.SettlementCache;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Cozy immortal stuffed-animal companion. Follows, sits, carries one trinket,
 * and offers gentle utilities — never combat help.
 */
public class StuffedPet extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_KIND =
            SynchedEntityData.defineId(StuffedPet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_GLOW_UTILITY =
            SynchedEntityData.defineId(StuffedPet.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_BUBBLE =
            SynchedEntityData.defineId(StuffedPet.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_RIBBON =
            SynchedEntityData.defineId(StuffedPet.class, EntityDataSerializers.INT);

    private static final int BUBBLE_DURATION = 50;
    private static final int FIND_COOLDOWN_TICKS = 20 * 60;
    private static final int GLOW_DURATION_TICKS = 20 * 30;
    private static final String[] AMBIENT_BUBBLES = {
            "♥", "...", "!", "warm", "soft", "yay", "hmm", "*snuggle*"
    };

    private ItemStack carried = ItemStack.EMPTY;
    private int bubbleTicks;
    private long findCooldownUntil;
    private int glowTicksRemaining;

    public StuffedPet(EntityType<? extends StuffedPet> type, Level level) {
        super(type, level);
        GroundPathNavigation navigation = (GroundPathNavigation) this.getNavigation();
        navigation.setCanFloat(true);
        this.setPathfindingMalus(PathType.FIRE, 16.0F);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, 0);
        builder.define(DATA_GLOW_UTILITY, false);
        builder.define(DATA_BUBBLE, "");
        builder.define(DATA_RIBBON, DyeColor.PINK.getId());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PetSitGoal(this));
        this.goalSelector.addGoal(2, new PetFollowOwnerGoal(this, 1.0, 6.0F, 2.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7) {
            @Override
            public boolean canUse() {
                return !StuffedPet.this.isOrderedToSit() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public PetKind getKind() {
        return PetKind.byId(this.entityData.get(DATA_KIND));
    }

    public void setKind(PetKind kind) {
        this.entityData.set(DATA_KIND, kind.ordinal());
    }

    public boolean isGlowUtility() {
        return this.entityData.get(DATA_GLOW_UTILITY);
    }

    public void setGlowUtility(boolean glowing) {
        this.entityData.set(DATA_GLOW_UTILITY, glowing);
    }

    public String getBubbleText() {
        return this.entityData.get(DATA_BUBBLE);
    }

    public void setBubbleText(String text) {
        this.entityData.set(DATA_BUBBLE, text == null ? "" : text);
        this.bubbleTicks = text == null || text.isEmpty() ? 0 : BUBBLE_DURATION;
    }

    public DyeColor getRibbonColor() {
        return DyeColor.byId(this.entityData.get(DATA_RIBBON));
    }

    public void setRibbonColor(DyeColor color) {
        this.entityData.set(DATA_RIBBON, color.getId());
    }

    public ItemStack getCarriedItem() {
        return carried;
    }

    public void awakenFor(ServerPlayer player, PetKind kind) {
        this.setKind(kind);
        this.tame(player);
        this.setOrderedToSit(false);
        this.setPersistenceRequired();
        this.setCustomName(Component.literal(kind.randomName(this.random)));
        this.setCustomNameVisible(true);
        this.setBubbleText("hello!");
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    this.getX(), this.getEyeY() + 0.3, this.getZ(), 8, 0.3, 0.3, 0.3, 0.0);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getEyeY(), this.getZ(), 12, 0.35, 0.35, 0.35, 0.02);
        }
        this.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 0.7F, 1.4F);

        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        data.setPet(this.getUUID(), kind.name());
        data.incrementPetsAwakened();
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        com.eva.evamod.adventure.AdventureService.signal(
                player, com.eva.evamod.adventure.AdventureService.Signal.AWAKEN_PET);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Immortal stuffed animal — take a nick, never go below 1 HP / die.
        float capped = Math.min(amount, Math.max(0.0F, this.getHealth() - 1.0F));
        boolean result = super.hurtServer(level, source, capped);
        if (this.getHealth() < 1.0F || this.isDeadOrDying()) {
            this.setHealth(1.0F);
        }
        if (capped > 0.0F) {
            level.sendParticles(ParticleTypes.POOF,
                    this.getX(), this.getEyeY(), this.getZ(), 2, 0.15, 0.15, 0.15, 0.01);
            this.setBubbleText("ow");
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        // Safety net: stuffed pets do not die.
        this.setHealth(1.0F);
        this.dead = false;
        this.deathTime = 0;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        return false;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKIE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEY_BOTTLE);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal partner) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!this.isTame()) {
            this.awakenFor(serverPlayer, this.getKind());
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!this.isOwnedBy(serverPlayer)) {
            serverPlayer.sendSystemMessage(Component.literal("This cozy friend already belongs to someone else.")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS_SERVER.withoutItem();
        }

        // Dye ribbon / cosmetic color
        DyeColor dye = stack.get(DataComponents.DYE);
        if (dye != null) {
            if (dye != this.getRibbonColor()) {
                this.setRibbonColor(dye);
                stack.consume(1, player);
                this.setBubbleText("pretty!");
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getEyeY() + 0.2, this.getZ(), 6, 0.25, 0.25, 0.25, 0.0);
                }
                this.playSound(SoundEvents.DYE_USE, 1.0F, 1.2F);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        // Glow berry toggles soft glow utility
        if (stack.is(Items.GLOW_BERRIES)) {
            stack.consume(1, player);
            this.toggleGlow(serverPlayer);
            return InteractionResult.SUCCESS_SERVER;
        }

        // Treats: bond hearts (immortal — no real heal needed)
        if (isFood(stack)) {
            if (!player.getAbilities().instabuild) {
                if (stack.is(Items.HONEY_BOTTLE)) {
                    stack.shrink(1);
                    if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                } else {
                    stack.shrink(1);
                }
            }
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getEyeY() + 0.35, this.getZ(), 5, 0.3, 0.25, 0.3, 0.0);
            }
            this.setBubbleText("yum");
            this.playSound(SoundEvents.GENERIC_EAT.value(), 0.6F, 1.35F);
            return InteractionResult.SUCCESS_SERVER;
        }

        // Sneak + item: store/swap one trinket; sneak + empty: retrieve
        if (player.isShiftKeyDown()) {
            if (stack.isEmpty()) {
                if (!carried.isEmpty()) {
                    ItemStack given = carried.copy();
                    carried = ItemStack.EMPTY;
                    if (!player.getInventory().add(given)) {
                        player.drop(given, false);
                    }
                    this.setBubbleText("here");
                    serverPlayer.sendSystemMessage(Component.literal("Your pet returns ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(given.getHoverName()));
                    return InteractionResult.SUCCESS_SERVER.withoutItem();
                }
            } else {
                ItemStack previous = carried.copy();
                carried = stack.copyWithCount(1);
                stack.shrink(1);
                if (!previous.isEmpty()) {
                    if (!player.getInventory().add(previous)) {
                        player.drop(previous, false);
                    }
                    serverPlayer.sendSystemMessage(Component.literal("Swapped trinket — now carrying ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(carried.getHoverName()));
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("Your pet tucked away ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(carried.getHoverName()));
                }
                this.setBubbleText("got it");
                this.playSound(SoundEvents.ITEM_PICKUP, 0.5F, 1.5F);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        // Empty hand: toggle sit / follow tip
        if (stack.isEmpty()) {
            boolean sit = !this.isOrderedToSit();
            this.setOrderedToSit(sit);
            this.jumping = false;
            this.getNavigation().stop();
            this.setBubbleText(sit ? "sit" : "ok!");
            serverPlayer.sendSystemMessage(Component.literal(sit
                            ? this.getName().getString() + " sits quietly."
                            : this.getName().getString() + " follows you again.")
                    .withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS_SERVER.withoutItem();
        }

        serverPlayer.sendSystemMessage(Component.literal(
                        "Empty hand: sit/follow · Dye: ribbon · Treats: hearts · Glow Berry: glow · Sneak+item: carry trinket")
                .withStyle(ChatFormatting.GRAY));
        return InteractionResult.SUCCESS_SERVER.withoutItem();
    }

    public void toggleGlow(ServerPlayer player) {
        boolean enable = !this.isGlowUtility();
        this.setGlowUtility(enable);
        if (enable) {
            this.glowTicksRemaining = GLOW_DURATION_TICKS;
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false));
            this.setBubbleText("glow!");
            player.sendSystemMessage(Component.literal(this.getName().getString() + " soft-glows for a little while.")
                    .withStyle(ChatFormatting.YELLOW));
            com.eva.evamod.adventure.AdventureService.signal(
                    player, com.eva.evamod.adventure.AdventureService.Signal.PET_GLOW);
        } else {
            this.glowTicksRemaining = 0;
            this.removeEffect(MobEffects.GLOWING);
            this.setBubbleText("...");
            player.sendSystemMessage(Component.literal(this.getName().getString() + " dims again.")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public int findSettlementHint(ServerPlayer player) {
        if (!(this.level() instanceof ServerLevel level)) {
            return 0;
        }
        long now = level.getGameTime();
        if (now < findCooldownUntil) {
            long left = (findCooldownUntil - now + 19) / 20;
            player.sendSystemMessage(Component.literal("Still sniffing the breeze… try again in " + left + "s.")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        findCooldownUntil = now + FIND_COOLDOWN_TICKS;

        BlockPos origin = this.blockPosition();
        BlockPos target = findNearestSettlement(level, origin);
        if (target == null) {
            this.setBubbleText("?");
            player.sendSystemMessage(Component.literal(
                            "No Eva settlement nearby yet. Explore a bit, then ask again — or try /evamod town.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        this.setOrderedToSit(false);
        this.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.05);
        this.setBubbleText("that way!");
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getEyeY() + 0.4, this.getZ(), 16, 0.4, 0.4, 0.4, 0.03);
        // Soft particle breadcrumb toward the settlement
        double dx = target.getX() + 0.5 - this.getX();
        double dz = target.getZ() + 0.5 - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0.001) {
            dx /= len;
            dz /= len;
            for (int i = 1; i <= 8; i++) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + dx * i * 1.2, this.getEyeY() + 0.2, this.getZ() + dz * i * 1.2,
                        1, 0.02, 0.02, 0.02, 0.0);
            }
        }

        player.sendSystemMessage(Component.literal(this.getName().getString() + " points toward a settlement near ")
                .withStyle(ChatFormatting.GREEN)
                .append(SettlementLocator.clickablePos(Component.empty(), target)));
        com.eva.evamod.adventure.AdventureService.signal(
                player, com.eva.evamod.adventure.AdventureService.Signal.PET_FIND);
        return 1;
    }

    private static @Nullable BlockPos findNearestSettlement(ServerLevel level, BlockPos origin) {
        SettlementCache cache = level.getData(ModAttachments.SETTLEMENT_CACHE);
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (List<BlockPos> list : List.of(cache.towns(), cache.houses())) {
            for (BlockPos pos : list) {
                double d = pos.distSqr(origin);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos;
                }
            }
        }
        BlockPos mapped = level.findNearestMapStructure(
                EvaModCommands.NPC_SETTLEMENT_TAG, origin, 64, false);
        if (mapped != null) {
            double d = mapped.distSqr(origin);
            if (d < bestDist) {
                best = mapped;
            }
        }
        BlockPos town = level.findNearestMapStructure(
                EvaModCommands.NPC_TOWN_TAG, origin, 96, false);
        if (town != null) {
            double d = town.distSqr(origin);
            if (best == null || d < best.distSqr(origin)) {
                best = town;
            }
        }
        return best;
    }

    public void softTeleportToOwner(ServerPlayer player) {
        this.setOrderedToSit(false);
        this.teleportTo(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
        this.resetFallDistance();
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.getNavigation().stop();
        this.setBubbleText("here!");
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    this.getX(), this.getY() + 0.4, this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (bubbleTicks > 0) {
            bubbleTicks--;
            if (bubbleTicks <= 0) {
                this.entityData.set(DATA_BUBBLE, "");
            }
        }
        if (glowTicksRemaining > 0) {
            glowTicksRemaining--;
            if (glowTicksRemaining <= 0) {
                this.setGlowUtility(false);
            } else if (this.tickCount % 12 == 0 && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getEyeY(), this.getZ(), 1, 0.15, 0.15, 0.15, 0.0);
            }
        }
        if (this.tickCount % 80 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
        }
        if (this.getY() < this.level().getMinY() - 8) {
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
            this.resetFallDistance();
        }
        if (this.random.nextInt(500) == 0) {
            this.setBubbleText(AMBIENT_BUBBLES[this.random.nextInt(AMBIENT_BUBBLES.length)]);
            if (this.level() instanceof ServerLevel serverLevel && this.random.nextBoolean()) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getEyeY() + 0.4, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
        // Show carried item in hand for a tiny bit of charm
        if (!carried.isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, carried.copyWithCount(1));
        } else if (!this.getMainHandItem().isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Nullable
    public static StuffedPet findOwnedPet(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        Optional<UUID> id = data.petEntityUuid();
        if (id.isPresent()) {
            net.minecraft.world.entity.Entity entity = player.level().getEntity(id.get());
            if (entity == null && player.level() instanceof ServerLevel serverLevel) {
                entity = serverLevel.getEntityInAnyDimension(id.get());
            }
            if (entity instanceof StuffedPet pet && pet.isOwnedBy(player)) {
                return pet;
            }
        }
        return findNearbyOwned(player);
    }

    private static @Nullable StuffedPet findNearbyOwned(ServerPlayer player) {
        return player.level().getEntitiesOfClass(StuffedPet.class, player.getBoundingBox().inflate(64.0),
                        pet -> pet.isOwnedBy(player))
                .stream()
                .min(Comparator.comparingDouble(pet -> pet.distanceToSqr(player)))
                .orElse(null);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("PetKind", this.getKind().ordinal());
        output.putBoolean("GlowUtility", this.isGlowUtility());
        output.putInt("Ribbon", this.getRibbonColor().getId());
        output.putLong("FindCooldownUntil", findCooldownUntil);
        output.putInt("GlowTicks", glowTicksRemaining);
        if (!carried.isEmpty()) {
            output.store("Carried", ItemStack.CODEC, carried);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setKind(PetKind.byId(input.getIntOr("PetKind", 0)));
        this.setGlowUtility(input.getBooleanOr("GlowUtility", false));
        this.setRibbonColor(DyeColor.byId(input.getIntOr("Ribbon", DyeColor.PINK.getId())));
        findCooldownUntil = input.getLongOr("FindCooldownUntil", 0L);
        glowTicksRemaining = input.getIntOr("GlowTicks", 0);
        carried = input.read("Carried", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (this.isGlowUtility() && glowTicksRemaining > 0) {
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicksRemaining, 0, false, false));
        }
    }
}
