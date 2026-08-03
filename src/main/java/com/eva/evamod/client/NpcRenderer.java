package com.eva.evamod.client;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.BiomeNpc;
import com.eva.evamod.entity.NpcVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class NpcRenderer extends HumanoidMobRenderer<BiomeNpc, NpcRenderState, HumanoidModel<NpcRenderState>> {
    private static final Map<NpcVariant, Identifier> TEXTURES = new EnumMap<>(NpcVariant.class);
    /** Extra lift so the bubble sits above the normal nametag, not replacing it. */
    private static final float BUBBLE_LIFT = 0.35F;

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

    @Override
    public void submit(NpcRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        submitBubble(state, poseStack, submitNodeCollector, camera);
    }

    private void submitBubble(NpcRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                              CameraRenderState camera) {
        if (state.bubbleText == null || state.bubbleText.isEmpty()) {
            return;
        }
        Vec3 attachment = state.nameTagAttachment;
        if (attachment == null) {
            attachment = new Vec3(0.0, state.boundingBoxHeight + 0.5, 0.0);
        }
        Component bubble = Component.literal("~ " + state.bubbleText);
        poseStack.pushPose();
        poseStack.translate(0.0F, BUBBLE_LIFT, 0.0F);
        // If a nametag is also showing, stack the bubble one line higher.
        if (state.nameTag != null) {
            poseStack.translate(0.0F, 9.0F * 1.15F * NAMETAG_SCALE, 0.0F);
        }
        submitNodeCollector.submitNameTag(
                poseStack, attachment, 0, bubble, !state.isDiscrete, state.lightCoords, camera);
        poseStack.popPose();
    }
}
