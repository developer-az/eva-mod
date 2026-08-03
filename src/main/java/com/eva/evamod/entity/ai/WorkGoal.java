package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcJob;
import com.eva.evamod.entity.SpeechBubbles;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Daytime job routine: walk to a work spot near home and putter around.
 */
public class WorkGoal extends Goal {
    private static final int WORK_DURATION = 400;
    private static final int SEARCH_RADIUS = 8;

    private final BiomeNpc npc;
    private final double speed;
    @Nullable
    private BlockPos workPos;
    private int workTicks;
    private int recalcCooldown;

    public WorkGoal(BiomeNpc npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!npc.hasHome() || npc.level().isDarkOutside() || npc.isSleeping()) {
            return false;
        }
        if (npc.level().isRaining() && npc.level().canSeeSky(npc.blockPosition())) {
            return false;
        }
        if (npc.getRandom().nextInt(reducedTickDelay(160)) != 0) {
            return false;
        }
        this.workPos = findWorkSpot();
        return this.workPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return workTicks > 0 && !npc.level().isDarkOutside() && !npc.level().isRaining() && workPos != null;
    }

    @Override
    public void start() {
        this.workTicks = WORK_DURATION;
        this.recalcCooldown = 0;
    }

    @Override
    public void stop() {
        this.workPos = null;
        npc.getNavigation().stop();
        npc.clearRestPose();
    }

    @Override
    public void tick() {
        if (workPos == null) {
            return;
        }
        workTicks--;
        double distSqr = npc.distanceToSqr(workPos.getX() + 0.5, workPos.getY() + 0.5, workPos.getZ() + 0.5);
        if (distSqr > 6.25) {
            npc.clearRestPose();
            if (--recalcCooldown <= 0) {
                recalcCooldown = this.adjustedTickDelay(40);
                npc.getNavigation().moveTo(workPos.getX() + 0.5, workPos.getY(), workPos.getZ() + 0.5, speed);
            }
        } else {
            npc.getNavigation().stop();
            npc.getLookControl().setLookAt(workPos.getX() + 0.5, workPos.getY() + 0.5, workPos.getZ() + 0.5);
            if (npc.getJob() == NpcJob.FARMER || npc.getJob() == NpcJob.HERBALIST) {
                npc.setCrouchWorking(true);
            } else {
                npc.tryRestPoseNear(workPos);
            }
            if (workTicks % 25 == 0) {
                npc.swing(InteractionHand.MAIN_HAND);
            }
            if (workTicks % 80 == 0) {
                npc.trySay(SpeechBubbles.Kind.IDLE_WORK, 80);
            }
        }
    }

    @Nullable
    private BlockPos findWorkSpot() {
        BlockPos home = npc.getHomePos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int attempts = 10;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = home.getX() + npc.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = home.getZ() + npc.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            for (int dy = 2; dy >= -2; dy--) {
                cursor.set(x, home.getY() + dy, z);
                if (npc.getJob().isWorkTarget(npc.level().getBlockState(cursor))) {
                    return cursor.immutable();
                }
            }
        }
        int x = home.getX() + npc.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
        int z = home.getZ() + npc.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
        cursor.set(x, home.getY(), z);
        if (npc.level().isEmptyBlock(cursor) || npc.level().isEmptyBlock(cursor.above())) {
            return cursor.immutable();
        }
        return null;
    }
}
