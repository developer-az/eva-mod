package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.StuffedPet;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

/** Follow the owner at a cozy stroll pace. */
public class PetFollowOwnerGoal extends FollowOwnerGoal {
    public PetFollowOwnerGoal(StuffedPet pet, double speed, float startDistance, float stopDistance) {
        super(pet, speed, startDistance, stopDistance);
    }
}
