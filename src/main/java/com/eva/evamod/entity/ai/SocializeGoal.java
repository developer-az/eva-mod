package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.SpeechBubbles;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

/**
 * Walk up to a neighboring BiomeNpc, face them and chat briefly.
 */
public class SocializeGoal extends Goal {
    private static final int CHAT_DURATION = 100;
    private static final double SEARCH_RANGE = 22.0;

    private final BiomeNpc npc;
    private final double speed;
    @Nullable
    private BiomeNpc friend;
    private int chatTicks;

    public SocializeGoal(BiomeNpc npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (npc.level().isDarkOutside() || npc.isSleeping()) {
            return false;
        }
        if (npc.level().getGameTime() < npc.nextSocializeTime) {
            return false;
        }
        if (npc.getRandom().nextInt(reducedTickDelay(npc.getPersonality().socializeRarity())) != 0) {
            return false;
        }
        this.friend = findFriend();
        return this.friend != null;
    }

    @Override
    public boolean canContinueToUse() {
        return friend != null && friend.isAlive() && chatTicks > 0 && !npc.level().isDarkOutside();
    }

    @Override
    public void start() {
        this.chatTicks = CHAT_DURATION;
        npc.nextSocializeTime = npc.level().getGameTime() + 1600L;
        if (friend != null) {
            npc.getNavigation().moveTo(friend, speed);
        }
    }

    @Override
    public void stop() {
        this.friend = null;
        npc.getNavigation().stop();
        npc.clearRestPose();
    }

    @Override
    public void tick() {
        if (friend == null) {
            return;
        }
        npc.getLookControl().setLookAt(friend, 30.0F, 30.0F);
        double distSqr = npc.distanceToSqr(friend);
        if (distSqr > 6.25) {
            npc.getNavigation().moveTo(friend, speed);
        } else {
            npc.getNavigation().stop();
            chatTicks--;
            if (chatTicks % 40 == 0) {
                npc.trySay(SpeechBubbles.Kind.SOCIAL, 60);
                if (npc.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            npc.getX(), npc.getEyeY() + 0.3, npc.getZ(), 1, 0.15, 0.15, 0.15, 0.0);
                }
            }
        }
    }

    @Nullable
    private BiomeNpc findFriend() {
        AABB box = npc.getBoundingBox().inflate(SEARCH_RANGE);
        List<BiomeNpc> neighbors = npc.level().getEntitiesOfClass(BiomeNpc.class, box,
                other -> other != npc && other.isAlive() && !other.isSleeping());
        if (neighbors.isEmpty()) {
            return null;
        }
        return neighbors.get(npc.getRandom().nextInt(neighbors.size()));
    }
}
