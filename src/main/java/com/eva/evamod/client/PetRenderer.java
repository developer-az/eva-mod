package com.eva.evamod.client;

import com.eva.evamod.EvaMod;
import com.eva.evamod.entity.StuffedPet;
import com.eva.evamod.pet.PetKind;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class PetRenderer extends HumanoidMobRenderer<StuffedPet, PetRenderState, HumanoidModel<PetRenderState>> {
    private static final Map<PetKind, Identifier> TEXTURES = new EnumMap<>(PetKind.class);
    private static final float BUBBLE_LIFT = 0.2F;

    static {
        for (PetKind kind : PetKind.values()) {
            TEXTURES.put(kind, Identifier.fromNamespaceAndPath(EvaMod.MODID,
                    "textures/entity/pet_" + kind.getTextureSuffix() + ".png"));
        }
    }

    public PetRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.3F);
    }

    @Override
    public PetRenderState createRenderState() {
        return new PetRenderState();
    }

    @Override
    public void extractRenderState(StuffedPet entity, PetRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.getKind();
        state.bubbleText = entity.getBubbleText();
        state.ribbonColor = entity.getRibbonColor();
        state.sitting = entity.isInSittingPose();
    }

    @Override
    public Identifier getTextureLocation(PetRenderState state) {
        return TEXTURES.get(state.kind);
    }

    @Override
    protected void scale(PetRenderState state, PoseStack poseStack) {
        float s = state.kind != null ? state.kind.getRenderScale() : 0.55F;
        poseStack.scale(s, s, s);
        if (state.sitting) {
            poseStack.translate(0.0F, 0.15F, 0.0F);
        }
    }

    @Override
    public void submit(PetRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        submitBubble(state, poseStack, submitNodeCollector, camera);
    }

    private void submitBubble(PetRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                              CameraRenderState camera) {
        if (state.bubbleText == null || state.bubbleText.isEmpty()) {
            return;
        }
        Vec3 attachment = state.nameTagAttachment;
        if (attachment == null) {
            attachment = new Vec3(0.0, state.boundingBoxHeight + 0.35, 0.0);
        }
        Component bubble = Component.literal("~ " + state.bubbleText);
        poseStack.pushPose();
        poseStack.translate(0.0F, BUBBLE_LIFT, 0.0F);
        if (state.nameTag != null) {
            poseStack.translate(0.0F, 9.0F * 1.15F * NAMETAG_SCALE, 0.0F);
        }
        submitNodeCollector.submitNameTag(
                poseStack, attachment, 0, bubble, !state.isDiscrete, state.lightCoords, camera);
        poseStack.popPose();
    }
}
