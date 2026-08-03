package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

/**
 * At night (or when raining outdoors), head for the home bed and go to sleep.
 * Sleepy personalities also take short afternoon naps.
 */
public class SleepGoal extends Goal {
    private static final int NAP_LENGTH = 200;

    private final BiomeNpc npc;
    private final double speed;
    @Nullable
    private BlockPos bedPos;
    private int settleTicks;
    private boolean nap;
    private int napTicksLeft;
    private long lastScanGameTime = Long.MIN_VALUE;

    public SleepGoal(BiomeNpc npc, double speed) {
        this.npc = npc;
        this.speed = speed;
        // Do not claim JUMP — pathfinding needs it for 1-block ledges near doors/steps.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (npc.isTrading()) {
            return false;
        }
        if (npc.isSleeping()) {
            return true;
        }
        if (!npc.hasHome()) {
            return false;
        }
        boolean night = npc.level().isDarkOutside();
        boolean rainyAndOutside = npc.level().isRaining() && npc.level().canSeeSky(npc.blockPosition());
        boolean napTime = npc.getPersonality().takesNaps()
                && npc.level().isBrightOutside()
                && !npc.level().isRaining()
                && npc.getRandom().nextInt(reducedTickDelay(2400)) == 0;
        if (!night && !rainyAndOutside && !napTime) {
            return false;
        }
        this.nap = napTime && !night;
        this.bedPos = resolveBed();
        return this.bedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (npc.isTrading()) {
            return false;
        }
        if (npc.isSleeping()) {
            if (nap) {
                return napTicksLeft > 0;
            }
            return npc.level().isDarkOutside() || npc.level().isRaining();
        }
        return bedPos != null && (nap || npc.level().isDarkOutside() || npc.level().isRaining());
    }

    @Override
    public void start() {
        this.settleTicks = 0;
        this.napTicksLeft = NAP_LENGTH;
        npc.prepareToPath();
        if (bedPos != null) {
            npc.getNavigation().moveTo(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, speed);
        }
    }

    @Override
    public void stop() {
        if (npc.isSleeping()) {
            npc.stopSleeping();
        }
        this.bedPos = null;
        this.nap = false;
        npc.getNavigation().stop();
        npc.clearRestPose();
    }

    @Override
    public void tick() {
        if (bedPos == null) {
            return;
        }
        if (npc.isSleeping()) {
            if (nap) {
                napTicksLeft--;
            }
            return;
        }
        double distSqr = npc.distanceToSqr(bedPos.getX() + 0.5, bedPos.getY() + 0.5, bedPos.getZ() + 0.5);
        if (distSqr > 2.25) {
            if (npc.getNavigation().isDone()) {
                Vec3 toward = DefaultRandomPos.getPosTowards(npc,
                        8, 4, Vec3.atBottomCenterOf(bedPos), Math.PI / 2.0);
                if (toward != null) {
                    npc.getNavigation().moveTo(toward.x, toward.y, toward.z, speed);
                } else {
                    npc.getNavigation().moveTo(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, speed);
                }
            }
        } else {
            settleTicks++;
            npc.getNavigation().stop();
            npc.tryRestPoseNear(bedPos);
            if (settleTicks > 20) {
                npc.startSleeping(bedPos);
            }
        }
    }

    @Nullable
    private BlockPos resolveBed() {
        BlockPos cached = npc.getCachedBedPos();
        if (cached != null && isBedHead(cached)) {
            return cached;
        }
        long time = npc.level().getGameTime();
        if (time - lastScanGameTime < 100L && cached != null) {
            return cached;
        }
        lastScanGameTime = time;
        BlockPos found = findBedNearHome();
        npc.setCachedBedPos(found);
        return found;
    }

    private boolean isBedHead(BlockPos pos) {
        BlockState state = npc.level().getBlockState(pos);
        return state.getBlock() instanceof BedBlock && state.hasProperty(BedBlock.PART)
                && state.getValue(BedBlock.PART) == BedPart.HEAD;
    }

    @Nullable
    private BlockPos findBedNearHome() {
        BlockPos home = npc.getHomePos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        // Compact scan: house interiors are small; avoid huge O(n^3) every canUse.
        for (int dy = 0; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    cursor.set(home.getX() + dx, home.getY() + dy, home.getZ() + dz);
                    if (isBedHead(cursor)) {
                        return cursor.immutable();
                    }
                }
            }
        }
        return home;
    }
}
