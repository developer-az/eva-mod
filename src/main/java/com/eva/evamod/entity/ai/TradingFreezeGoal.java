package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.BiomeNpc;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** While a trade UI is open: stand still and face the trading player. */
public class TradingFreezeGoal extends Goal {
    private final BiomeNpc npc;

    public TradingFreezeGoal(BiomeNpc npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return npc.isTrading();
    }

    @Override
    public boolean canContinueToUse() {
        return npc.isTrading();
    }

    @Override
    public void start() {
        npc.getNavigation().stop();
        npc.clearRestPose();
    }

    @Override
    public void tick() {
        Player trader = npc.getTradingPlayer();
        npc.getNavigation().stop();
        if (trader != null) {
            npc.getLookControl().setLookAt(trader, 30.0F, 30.0F);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
