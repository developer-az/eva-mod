package com.eva.evamod.client;

import com.eva.evamod.pet.PetKind;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.DyeColor;

public class PetRenderState extends HumanoidRenderState {
    public PetKind kind = PetKind.TEDDY;
    public String bubbleText = "";
    public DyeColor ribbonColor = DyeColor.PINK;
    public boolean sitting;
}
