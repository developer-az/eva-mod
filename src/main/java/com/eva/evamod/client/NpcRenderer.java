package com.eva.evamod.client;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcVariant;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

public class NpcRenderer extends HumanoidMobRenderer<BiomeNpc, NpcRenderState, HumanoidModel<NpcRenderState>> {
    private static final Map<NpcVariant, Identifier> TEXTURES = new EnumMap<>(NpcVariant.class);

    static {
        for (NpcVariant variant : NpcVariant.values()) {
            TEXTURES.put(variant, Identifier.fromNamespaceAndPath(EvaMod.MODID,
                    "textures/entity/npc_" + variant.name().toLowerCase(Locale.ROOT) + ".png"));
        }
    }

    public NpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public NpcRenderState createRenderState() {
        return new NpcRenderState();
    }

    @Override
    public void extractRenderState(BiomeNpc entity, NpcRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.bubbleText = entity.getBubbleText();
    }

    @Override
    public Identifier getTextureLocation(NpcRenderState state) {
        return TEXTURES.get(state.variant);
    }

    @Override
    protected void scale(NpcRenderState state, PoseStack poseStack) {
        // Slightly shorter visual so they read as door-friendly neighbors.
        poseStack.scale(0.94F, 0.94F, 0.94F);
    }
}
