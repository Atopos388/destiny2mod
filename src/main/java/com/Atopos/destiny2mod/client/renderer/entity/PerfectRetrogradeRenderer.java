package com.Atopos.destiny2mod.client.renderer.entity;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.client.model.PerfectRetrogradeModel;
import com.Atopos.destiny2mod.entity.custom.PerfectRetrogradeProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PerfectRetrogradeRenderer extends EntityRenderer<PerfectRetrogradeProjectile> {
    // [修复] 使用单参数构造函数修复过时警告
    private static final ResourceLocation TEXTURE = new ResourceLocation(Destiny2Mod.MODID + ":textures/entity/perfect_retrograde_projectile.png");
    private final PerfectRetrogradeModel<PerfectRetrogradeProjectile> model;

    public PerfectRetrogradeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PerfectRetrogradeModel<>(context.bakeLayer(PerfectRetrogradeModel.LAYER_LOCATION));
    }

    @Override
    public void render(PerfectRetrogradeProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 平滑获取旋转角度
        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        // 2. 核心旋转逻辑
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

        // 3. 基础修正
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -1.5F, 0.0F);

        // 如果模型在 Blockbench 里是沿着 X 轴画的，这里可能需要开启 90 度修正
        // poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PerfectRetrogradeProjectile entity) {
        return TEXTURE;
    }
}