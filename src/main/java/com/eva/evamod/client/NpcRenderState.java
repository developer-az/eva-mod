package com.eva.evamod.client;

import com.eva.evamod.entity.NpcVariant;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class NpcRenderState extends HumanoidRenderState {
    public NpcVariant variant = NpcVariant.PLAINS;
    public String bubbleText = "";
}
