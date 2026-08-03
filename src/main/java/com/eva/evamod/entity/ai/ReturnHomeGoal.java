package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Sends the NPC back to its house at night (or when it wandered way too far).
 */
public class ReturnHomeGoal extends Goal {
    private final BiomeNpc npc;
    private final double speed;
    private int recalcCooldown;

    public ReturnHomeGoal(BiomeNpc npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private double distToHomeSqr() {
        BlockPos home = npc.getHomePos();
        return npc.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
    }

    private boolean wantsToGoHome() {
        if (!npc.hasHome() || npc.isSleeping()) {
            return false;
        }
        double distSqr = distToHomeSqr();
        // Rain: dash home for cover if outdoors
        if (npc.level().isRaining() && npc.level().canSeeSky(npc.blockPosition())) {
            return distSqr > 4.0;
        }
        if (npc.level().isDarkOutside()) {
            return distSqr > 6.0;
        }
        return distSqr > (BiomeNpc.HOME_RADIUS + 8) * (BiomeNpc.HOME_RADIUS + 8);
    }

    @Override
    public boolean canUse() {
        return !npc.isTrading() && wantsToGoHome();
    }

    @Override
    public boolean canContinueToUse() {
        return !npc.isTrading() && wantsToGoHome() && !npc.getNavigation().isDone();
    }

    @Override
    public void start() {
        npc.prepareToPath();
        moveToHome();
    }

    @Override
    public void tick() {
        if (--recalcCooldown <= 0) {
            moveToHome();
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
    }

    private void moveToHome() {
        recalcCooldown = this.adjustedTickDelay(60);
        BlockPos home = npc.getHomePos();
        npc.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, speed);
    }
}
