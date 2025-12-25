package com.Atopos.destiny2mod.client.renderer.entity;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.client.model.SolarFlareModel;
import com.Atopos.destiny2mod.entity.custom.SolarFlareEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SolarFlareRenderer extends EntityRenderer<SolarFlareEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Destiny2Mod.MODID, "textures/entity/solar_flare.png");
    private final SolarFlareModel<SolarFlareEntity> model;

    public SolarFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 从客户端层烘焙模型
        this.model = new SolarFlareModel<>(context.bakeLayer(SolarFlareModel.LAYER_LOCATION));
    }

    @Override
    public void render(SolarFlareEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 旋转修正
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -1.5F, 0.0F);

        float ageInTicks = entity.tickCount + partialTick;
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));

        // 应用动画
        this.model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SolarFlareEntity entity) {
        return TEXTURE;
    }
}