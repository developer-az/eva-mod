package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.SpeechBubbles;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Cozy home protection: when a hostile lingers near the house, the NPC scares it off
 * with knockback, a light tap, and brief Weakness — not an OP murder machine.
 */
public class DefendHomeGoal extends Goal {
    private static final double DEFEND_RANGE = 14.0;
    private static final double STRIKE_RANGE = 2.4;
    private static final int STRIKE_COOLDOWN = 35;

    private final BiomeNpc npc;
    private final double speed;
    @Nullable
    private LivingEntity target;
    private int seeTime;
    private int cooldown;

    public DefendHomeGoal(BiomeNpc npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!npc.hasHome() || npc.isTrading() || npc.isSleeping()) {
            return false;
        }
        if (npc.distanceToSqr(Vec3.atCenterOf(npc.getHomePos())) > BiomeNpc.HOME_RADIUS * BiomeNpc.HOME_RADIUS) {
            return false;
        }
        this.target = findHostile();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (npc.isTrading() || npc.isSleeping() || !npc.hasHome()) {
            return false;
        }
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (npc.distanceToSqr(Vec3.atCenterOf(npc.getHomePos())) > (BiomeNpc.HOME_RADIUS + 4)
                * (BiomeNpc.HOME_RADIUS + 4)) {
            return false;
        }
        return target.distanceToSqr(npc.getHomePos().getX() + 0.5, npc.getHomePos().getY(),
                npc.getHomePos().getZ() + 0.5) <= DEFEND_RANGE * DEFEND_RANGE;
    }

    @Override
    public void start() {
        this.seeTime = 0;
        this.cooldown = 0;
        npc.prepareToPath();
        npc.trySay(SpeechBubbles.Kind.PLAYER_NEAR, 80);
    }

    @Override
    public void stop() {
        this.target = null;
        npc.getNavigation().stop();
        npc.clearRestPose();
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        npc.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = npc.distanceToSqr(target);
        if (dist > STRIKE_RANGE * STRIKE_RANGE) {
            if (npc.getNavigation().isDone()) {
                npc.getNavigation().moveTo(target, speed);
            }
            seeTime = 0;
        } else {
            npc.getNavigation().stop();
            seeTime++;
            if (cooldown > 0) {
                cooldown--;
            }
            if (seeTime > 8 && cooldown <= 0) {
                scareOff(target);
                cooldown = STRIKE_COOLDOWN;
                seeTime = 0;
            }
        }
    }

    private void scareOff(LivingEntity hostile) {
        if (!(npc.level() instanceof ServerLevel level)) {
            return;
        }
        // Tiny tap — immortal NPCs; hostiles get nudged, not melted.
        hostile.hurtServer(level, npc.damageSources().mobAttack(npc), 2.0F);
        Vec3 away = hostile.position().subtract(npc.position()).normalize().scale(0.55).add(0.0, 0.22, 0.0);
        hostile.setDeltaMovement(hostile.getDeltaMovement().add(away));
        hostile.hurtMarked = true;
        hostile.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, true));
        hostile.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true));
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                hostile.getX(), hostile.getEyeY(), hostile.getZ(), 4, 0.25, 0.25, 0.25, 0.0);
        level.sendParticles(ParticleTypes.CRIT,
                hostile.getX(), hostile.getY() + 0.6, hostile.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
        npc.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.9F + npc.getRandom().nextFloat() * 0.2F);
        npc.trySay(SpeechBubbles.Kind.WEATHER, 60);
    }

    @Nullable
    private LivingEntity findHostile() {
        AABB box = new AABB(npc.getHomePos()).inflate(DEFEND_RANGE);
        List<Mob> hostiles = npc.level().getEntitiesOfClass(Mob.class, box,
                m -> m instanceof Enemy && m.isAlive() && !m.isRemoved()
                        && m.distanceToSqr(npc) < DEFEND_RANGE * DEFEND_RANGE);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Mob monster : hostiles) {
            double d = monster.distanceToSqr(npc);
            if (d < bestDist) {
                bestDist = d;
                best = monster;
            }
        }
        return best;
    }
}
