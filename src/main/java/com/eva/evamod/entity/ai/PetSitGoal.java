package com.eva.evamod.entity.ai;

import com.eva.evamod.entity.StuffedPet;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;

/** Sit when the owner asks — thin TamableAnimal sit goal for stuffed pets. */
public class PetSitGoal extends SitWhenOrderedToGoal {
    public PetSitGoal(StuffedPet pet) {
        super(pet);
    }
}
