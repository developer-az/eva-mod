package com.eva.evamod.entity;

import com.eva.evamod.calendar.SeasonCalendar;
import com.eva.evamod.dialogue.DialogueManager;
import com.eva.evamod.entity.ai.ReturnHomeGoal;
import com.eva.evamod.entity.ai.SleepGoal;
import com.eva.evamod.entity.ai.SocializeGoal;
import com.eva.evamod.entity.ai.TradingFreezeGoal;
import com.eva.evamod.entity.ai.WorkGoal;
import com.eva.evamod.friendship.Hearts;
import com.eva.evamod.gift.GiftTaste;
import com.eva.evamod.memory.NpcMemory;
import com.eva.evamod.net.OpenDialoguePayload;
import com.eva.evamod.net.OpenTradePayload;
import com.eva.evamod.player.HouseIndexEntry;
import com.eva.evamod.player.PlayerEvaData;
import com.eva.evamod.quest.Errand;
import com.eva.evamod.registry.ModAttachments;
import com.eva.evamod.trade.NpcTrades;
import com.eva.evamod.world.UsedNpcNamesData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;

public class BiomeNpc extends PathfinderMob implements Merchant {
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(BiomeNpc.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_JOB =
            SynchedEntityData.defineId(BiomeNpc.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_BUBBLE =
            SynchedEntityData.defineId(BiomeNpc.class, EntityDataSerializers.STRING);

    public static final int HOME_RADIUS = 24;
    private static final int FRIEND_REP_THRESHOLD = 40;
    private static final int BUBBLE_DURATION = 60;
    private static final int ERRAND_HEART_GATE = 2;

    private final NpcMemory memory = new NpcMemory();
    private BlockPos homePos = BlockPos.ZERO;
    private NpcPersonality personality = NpcPersonality.CHEERFUL;
    public long nextSocializeTime;
    private String lastGiftName = "something";
    private @Nullable BlockPos cachedBedPos;
    private int bubbleTicks;
    private long nextBubbleGameTime;
    private int stuckJumpCooldown;

    private @Nullable Player tradingPlayer;
    private @Nullable MerchantOffers offers;
    private long offersDay = Long.MIN_VALUE;
    private @Nullable MerchantOffer friendOffer;

    public BiomeNpc(EntityType<? extends BiomeNpc> type, Level level) {
        super(type, level);
        GroundPathNavigation navigation = (GroundPathNavigation) this.getNavigation();
        navigation.setCanOpenDoors(true);
        navigation.setCanFloat(true);
        navigation.getNodeEvaluator().setCanPassDoors(true);
        this.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, 0.0F);
        this.setPathfindingMalus(PathType.DOOR_IRON_CLOSED, -1.0F);
        this.setPathfindingMalus(PathType.FIRE, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_JOB, 0);
        builder.define(DATA_BUBBLE, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradingFreezeGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new SleepGoal(this, 0.9));
        this.goalSelector.addGoal(5, new ReturnHomeGoal(this, 1.0));
        this.goalSelector.addGoal(6, new SocializeGoal(this, 0.7));
        this.goalSelector.addGoal(7, new WorkGoal(this, 0.7));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    public NpcVariant getVariant() {
        return NpcVariant.byId(this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(NpcVariant variant) {
        this.entityData.set(DATA_VARIANT, variant.ordinal());
    }

    public NpcJob getJob() {
        return NpcJob.byId(this.entityData.get(DATA_JOB));
    }

    public void setJob(NpcJob job) {
        this.entityData.set(DATA_JOB, job.ordinal());
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(job.getWorkItem()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public NpcPersonality getPersonality() {
        return personality;
    }

    public boolean hasHome() {
        return !BlockPos.ZERO.equals(homePos);
    }

    public BlockPos getHomePos() {
        return homePos;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos.immutable();
        this.setHomeTo(this.homePos, HOME_RADIUS);
        this.cachedBedPos = null;
    }

    public @Nullable BlockPos getCachedBedPos() {
        return cachedBedPos;
    }

    public void setCachedBedPos(@Nullable BlockPos pos) {
        this.cachedBedPos = pos == null ? null : pos.immutable();
    }

    public String getNpcName() {
        Component name = this.getCustomName();
        return name != null ? name.getString() : "Wanderer";
    }

    public String getBubbleText() {
        return this.entityData.get(DATA_BUBBLE);
    }

    public long currentDay() {
        return this.level().getOverworldClockTime() / 24000L;
    }

    public NpcMemory getMemory() {
        return memory;
    }

    public Item getFavoriteGift() {
        return GiftTaste.personalLove(this.getUUID());
    }

    public String getFavoriteGiftName() {
        return new ItemStack(getFavoriteGift()).getHoverName().getString().toLowerCase(Locale.ROOT);
    }

    public String getLastGiftName() {
        return lastGiftName;
    }

    public int getBirthdayDayOfYear() {
        return SeasonCalendar.birthdayDayOfYear(this.getUUID().hashCode());
    }

    public String getBirthdayLabel() {
        return SeasonCalendar.formatBirthday(getBirthdayDayOfYear());
    }

    public boolean isBirthday(long day) {
        return SeasonCalendar.isBirthday(day, getBirthdayDayOfYear());
    }

    public int heartsWith(ServerPlayer player) {
        return Hearts.fromReputation(memory.get(player.getUUID(), currentDay()).reputation);
    }

    public void trySay(SpeechBubbles.Kind kind, int minCooldownTicks) {
        if (this.level().isClientSide() || this.isSleeping() || this.isTrading()) {
            return;
        }
        long time = this.level().getGameTime();
        if (time < nextBubbleGameTime) {
            return;
        }
        nextBubbleGameTime = time + Math.max(40, minCooldownTicks);
        this.entityData.set(DATA_BUBBLE, SpeechBubbles.pick(kind, this.random));
        this.bubbleTicks = BUBBLE_DURATION;
    }

    public void setCrouchWorking(boolean crouch) {
        if (this.isSleeping()) {
            return;
        }
        this.setPose(crouch ? Pose.CROUCHING : Pose.STANDING);
    }

    public void tryRestPoseNear(BlockPos focus) {
        if (this.isSleeping() || this.level().isClientSide()) {
            return;
        }
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        BlockState at = this.level().getBlockState(this.blockPosition());
        if (below.getBlock() instanceof StairBlock || at.getBlock() instanceof StairBlock
                || below.is(BlockTags.STAIRS) || at.is(BlockTags.STAIRS)) {
            this.setPose(Pose.SITTING);
        } else if (this.getJob() == NpcJob.FARMER || this.getJob() == NpcJob.HERBALIST) {
            this.setPose(Pose.CROUCHING);
        }
    }

    public void clearRestPose() {
        if (!this.isSleeping() && this.getPose() != Pose.STANDING && this.getPose() != Pose.SLEEPING) {
            this.setPose(Pose.STANDING);
        }
    }

    /** Stand up before pathing so crouch/sit poses never inflate door clearance checks. */
    public void prepareToPath() {
        if (!this.isSleeping()) {
            this.clearRestPose();
            this.setPose(Pose.STANDING);
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return switch (pose) {
            case SLEEPING -> EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(0.2F);
            case CROUCHING, SITTING -> EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.27F);
            default -> EntityDimensions.scalable(0.6F, 1.8F).withEyeHeight(1.62F);
        };
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        NpcVariant variant = NpcVariant.fromBiome(level.getBiome(this.blockPosition()));
        this.setVariant(variant);
        this.setJob(variant.randomJob(this.random));
        this.personality = NpcPersonality.random(this.random);
        this.setCustomName(Component.literal(UsedNpcNamesData.claim(level, variant, this.random)));
        this.setCustomNameVisible(true);
        if (!this.hasHome()) {
            this.setHomePos(this.blockPosition());
        }
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer player) {
            memory.recordHit(player.getUUID(), currentDay());
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    this.getX(), this.getEyeY() + 0.5, this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
        }
        float capped = Math.min(amount, Math.max(0.0F, this.getHealth() - 1.0F));
        boolean result = super.hurtServer(level, source, capped);
        if (this.getHealth() < 1.0F || this.isDeadOrDying()) {
            this.setHealth(1.0F);
        }
        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    public void stopTrading() {
        this.tradingPlayer = null;
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
        if (tradingPlayer != null) {
            if (!stillValid(tradingPlayer) || tradingPlayer.isSpectator()) {
                stopTrading();
            } else {
                this.getNavigation().stop();
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                this.clearRestPose();
                this.getLookControl().setLookAt(tradingPlayer, 30.0F, 30.0F);
                return;
            }
        }
        if (this.tickCount % 60 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
        }
        if (this.getY() < this.level().getMinY() - 8) {
            BlockPos target = this.hasHome() ? this.homePos : this.level().getLevelData().getRespawnData().pos();
            this.teleportTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5);
            this.resetFallDistance();
            this.setDeltaMovement(0.0, 0.0, 0.0);
        }
        maybeJumpLedge();
        maybeAmbientBubble();
        if (this.level() instanceof ServerLevel serverLevel && !this.isSleeping()) {
            if (personality == NpcPersonality.CHEERFUL && this.random.nextInt(900) == 0) {
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        this.getX(), this.getEyeY() + 0.6, this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            } else if (personality == NpcPersonality.SLEEPY && this.level().isBrightOutside()
                    && this.random.nextInt(1200) == 0) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX(), this.getEyeY() + 0.4, this.getZ(), 1, 0.1, 0.08, 0.1, 0.01);
            }
        }
    }

    private void maybeJumpLedge() {
        if (this.isSleeping() || stuckJumpCooldown-- > 0) {
            return;
        }
        if (!this.onGround() || this.getNavigation().isDone()) {
            return;
        }
        if (this.horizontalCollision) {
            this.getJumpControl().jump();
            stuckJumpCooldown = 15;
        }
    }

    private void maybeAmbientBubble() {
        if (this.isSleeping() || this.random.nextInt(400) != 0) {
            return;
        }
        Player nearest = this.level().getNearestPlayer(this, 6.0);
        if (nearest != null) {
            trySay(SpeechBubbles.Kind.PLAYER_NEAR, 120);
        } else if (this.level().isRaining() && this.level().canSeeSky(this.blockPosition())) {
            trySay(SpeechBubbles.Kind.WEATHER, 160);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            this.clearRestPose();
            if (this.isSleeping()) {
                this.stopSleeping();
                this.openDialogue(serverPlayer, DialogueManager.Context.WOKEN);
            } else if (player.isShiftKeyDown() && !player.getItemInHand(hand).isEmpty()) {
                this.receiveGift(serverPlayer, player.getItemInHand(hand));
            } else {
                this.openDialogue(serverPlayer, DialogueManager.Context.GREETING);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void openDialogue(ServerPlayer player, DialogueManager.Context context) {
        long day = currentDay();
        NpcMemory.Record record = memory.get(player.getUUID(), day);
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        Errand active = data.activeErrand();

        // Heart events fire once per milestone when greeting/talking.
        if ((context == DialogueManager.Context.GREETING || context == DialogueManager.Context.SMALL_TALK)
                && record.moodTier() >= NpcMemory.MOOD_NEUTRAL) {
            int hearts = Hearts.fromReputation(record.reputation);
            if (Hearts.isMilestone(hearts)) {
                String key = this.getUUID() + ":heart:" + hearts;
                if (!data.hasSeenHeartEvent(key)) {
                    data.markHeartEvent(key);
                    player.setData(ModAttachments.PLAYER_DATA, data.copy());
                    context = DialogueManager.Context.HEART_EVENT;
                }
            }
        }

        String line = DialogueManager.pickLine(this, record, player.getName().getString(),
                context, day, this.random, player, active);
        if (context == DialogueManager.Context.GREETING
                || context == DialogueManager.Context.SMALL_TALK
                || context == DialogueManager.Context.HEART_EVENT
                || context == DialogueManager.Context.BIRTHDAY
                || context == DialogueManager.Context.FESTIVAL
                || context == DialogueManager.Context.ADVENTURE_TIP) {
            memory.recordTalk(player.getUUID(), day);
        }
        this.clearRestPose();
        this.getLookControl().setLookAt(player);
        rememberInHouseIndex(player);

        int hearts = Hearts.fromReputation(memory.get(player.getUUID(), day).reputation);
        boolean canErrand = hearts >= ERRAND_HEART_GATE
                && (active == null || active.completed() || active.matchesNpc(this.getUUID()));
        PacketDistributor.sendToPlayer(player, new OpenDialoguePayload(
                this.getId(), this.getNpcName(),
                personality.getDisplayName() + " " + this.getVariant().getDisplayName()
                        + " " + this.getJob().getDisplayName(),
                line, record.moodTier(), hearts, getBirthdayLabel(), canErrand));
    }

    private void rememberInHouseIndex(ServerPlayer player) {
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        int hearts = Hearts.fromReputation(memory.get(player.getUUID(), currentDay()).reputation);
        HouseIndexEntry entry = new HouseIndexEntry(
                this.getNpcName(),
                this.getJob().getDisplayName(),
                this.getPersonality().getDisplayName(),
                this.getVariant().getDisplayName(),
                this.hasHome() ? this.homePos : this.blockPosition(),
                hearts,
                getBirthdayLabel());
        data.meetNpc(entry);
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
    }

    private void receiveGift(ServerPlayer player, ItemStack stack) {
        long day = currentDay();
        NpcMemory.Record record = memory.get(player.getUUID(), day);
        this.lastGiftName = stack.getHoverName().getString().toLowerCase(Locale.ROOT);

        if (record.lastGiftDay == day) {
            this.openDialogue(player, DialogueManager.Context.GIFT_ALREADY);
            return;
        }

        GiftTaste taste = GiftTaste.rate(stack.getItem(), this.getUUID(), this.getJob(), this.personality);
        boolean birthday = isBirthday(day);
        int repChange = taste.reputationDelta();
        if (birthday && taste != GiftTaste.HATE && taste != GiftTaste.DISLIKE) {
            repChange = Math.max(repChange * 2, repChange + 10);
        }

        DialogueManager.Context reaction;
        if (birthday && (taste == GiftTaste.LOVE || taste == GiftTaste.LIKE)) {
            reaction = DialogueManager.Context.GIFT_BIRTHDAY;
        } else {
            reaction = switch (taste) {
                case LOVE -> DialogueManager.Context.GIFT_FAVORITE;
                case LIKE -> DialogueManager.Context.GIFT_LIKED;
                case NEUTRAL -> DialogueManager.Context.GIFT_MEH;
                case DISLIKE, HATE -> DialogueManager.Context.GIFT_JUNK;
            };
        }

        memory.recordGift(player.getUUID(), day, taste == GiftTaste.LOVE, repChange);
        if (!player.getAbilities().instabuild && repChange >= 0) {
            stack.shrink(1);
        } else if (!player.getAbilities().instabuild && taste == GiftTaste.DISLIKE) {
            // Disliked gifts still get taken (awkward), hated ones get refused.
            stack.shrink(1);
        }

        trySay(SpeechBubbles.Kind.GIFT, 40);

        if (this.level() instanceof ServerLevel serverLevel) {
            if (taste == GiftTaste.LOVE || birthday) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getEyeY() + 0.5, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
                this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
            } else if (repChange > 0) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getEyeY() + 0.5, this.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
                this.playSound(SoundEvents.VILLAGER_YES, this.getSoundVolume(), this.getVoicePitch());
            } else {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.getX(), this.getEyeY() + 0.5, this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
                this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
            }
        }
        this.openDialogue(player, reaction);
    }

    /** Offer, check, or complete an errand from the dialogue Help button. */
    public void handleErrand(ServerPlayer player) {
        long day = currentDay();
        PlayerEvaData data = player.getData(ModAttachments.PLAYER_DATA);
        Errand active = data.activeErrand();
        int hearts = heartsWith(player);

        if (active != null && !active.completed() && active.matchesNpc(this.getUUID())) {
            Item need = active.item();
            boolean paid = player.getAbilities().instabuild
                    || (need != Items.AIR && takeCost(player, new ItemStack(need, active.count())));
            if (need != Items.AIR && paid) {
                memory.recordGift(player.getUUID(), day, false, active.rewardRep());
                data.completeErrand();
                player.setData(ModAttachments.PLAYER_DATA, data.copy());
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART,
                            this.getX(), this.getEyeY() + 0.5, this.getZ(), 6, 0.35, 0.35, 0.35, 0.0);
                }
                this.openDialogue(player, DialogueManager.Context.ERRAND_COMPLETE);
            } else {
                this.openDialogue(player, DialogueManager.Context.ERRAND_PROGRESS);
            }
            return;
        }

        if (active != null && !active.completed() && !active.matchesNpc(this.getUUID())) {
            // Already helping someone else — remind them via progress line with that errand.
            this.openDialogue(player, DialogueManager.Context.ERRAND_PROGRESS);
            return;
        }

        if (hearts < ERRAND_HEART_GATE) {
            this.openDialogue(player, DialogueManager.Context.SMALL_TALK);
            return;
        }

        Errand errand = Errand.create(this.getNpcName(), this.getUUID(), day, this.random);
        data.setActiveErrand(errand);
        player.setData(ModAttachments.PLAYER_DATA, data.copy());
        this.openDialogue(player, DialogueManager.Context.ERRAND_OFFER);
    }

    public void startTrading(ServerPlayer player) {
        long day = currentDay();
        NpcMemory.Record record = memory.get(player.getUUID(), day);
        MerchantOffers current = getOrCreateOffers();

        if (friendOffer != null) {
            current.remove(friendOffer);
            friendOffer = null;
        }
        if (record.reputation >= FRIEND_REP_THRESHOLD) {
            friendOffer = NpcTrades.friendOffer(this.getJob());
            current.add(friendOffer);
        }

        for (MerchantOffer offer : current) {
            int baseCount = offer.getBaseCostA().getCount();
            int diff = -Mth.floor(record.reputation / 25.0F * Math.max(1, baseCount / 2));
            offer.setSpecialPriceDiff(diff);
        }

        this.setTradingPlayer(player);
        this.clearRestPose();
        rememberInHouseIndex(player);

        List<OpenTradePayload.TradeRow> rows = new ArrayList<>();
        for (MerchantOffer offer : current) {
            rows.add(new OpenTradePayload.TradeRow(
                    offer.getCostA(),
                    offer.getResult(),
                    offer.getUses(),
                    offer.getMaxUses()));
        }
        PacketDistributor.sendToPlayer(player, new OpenTradePayload(this.getId(), this.getNpcName(), rows));
    }

    public boolean performCustomTrade(ServerPlayer player, int offerIndex) {
        if (player.distanceToSqr(this) > 64.0) {
            return false;
        }
        MerchantOffers current = getOrCreateOffers();
        if (offerIndex < 0 || offerIndex >= current.size()) {
            return false;
        }
        MerchantOffer offer = current.get(offerIndex);
        if (offer.isOutOfStock()) {
            return false;
        }
        ItemStack cost = offer.getCostA();
        ItemStack result = offer.getResult().copy();
        if (cost.isEmpty() || result.isEmpty()) {
            return false;
        }
        if (!player.getAbilities().instabuild && !takeCost(player, cost)) {
            return false;
        }
        if (!player.getInventory().add(result.copy())) {
            player.drop(result.copy(), false);
        }
        offer.increaseUses();
        this.notifyTrade(offer);
        // Refresh client trade screen stock
        startTrading(player);
        return true;
    }

    private static boolean takeCost(ServerPlayer player, ItemStack cost) {
        int needed = cost.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize() && needed > 0; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, cost)) {
                int take = Math.min(needed, slot.getCount());
                slot.shrink(take);
                needed -= take;
            }
        }
        return needed <= 0;
    }

    private MerchantOffers getOrCreateOffers() {
        long day = currentDay();
        if (offers == null || offersDay != day) {
            offers = NpcTrades.buildDailyOffers(this.getJob(), day, this.getUUID());
            offersDay = day;
            friendOffer = null;
        }
        return offers;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override
    public @Nullable Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        return getOrCreateOffers();
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        if (tradingPlayer != null) {
            memory.recordTrade(tradingPlayer.getUUID(), currentDay());
        }
        this.playSound(SoundEvents.VILLAGER_YES, this.getSoundVolume(), this.getVoicePitch());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getEyeY(), this.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isAlive() && player.distanceToSqr(this) <= 64.0;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isSleeping() ? null : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant().ordinal());
        output.putInt("Job", this.getJob().ordinal());
        output.putInt("Personality", personality.ordinal());
        output.putInt("HomeX", homePos.getX());
        output.putInt("HomeY", homePos.getY());
        output.putInt("HomeZ", homePos.getZ());
        memory.save(output.child("NpcMemory"));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(NpcVariant.byId(input.getIntOr("Variant", 0)));
        this.entityData.set(DATA_JOB, Math.floorMod(input.getIntOr("Job", 0), NpcJob.values().length));
        this.personality = NpcPersonality.byId(input.getIntOr("Personality", 0));
        BlockPos home = new BlockPos(
                input.getIntOr("HomeX", 0),
                input.getIntOr("HomeY", 0),
                input.getIntOr("HomeZ", 0));
        if (!BlockPos.ZERO.equals(home)) {
            this.setHomePos(home);
        }
        memory.load(input.childOrEmpty("NpcMemory"));
    }
}
