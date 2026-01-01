package com.Atopos.destiny2mod.client.renderer.entity;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.entity.custom.WellOfRadianceEntity;
import com.Atopos.destiny2mod.init.ItemInit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WellOfRadianceRenderer extends EntityRenderer<WellOfRadianceEntity> {
    private static final ResourceLocation TEXTURE =     new ResourceLocation(Destiny2Mod.MODID, "textures/entity/solar_flare.png");
    private final ItemRenderer itemRenderer;
    
    public WellOfRadianceRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }
    
    @Override
    public void render(WellOfRadianceEntity entity, float entityYaw, float partialTick, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 只需要调用 super.render，因为我们不再渲染中间的剑，只依赖粒子效果
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(WellOfRadianceEntity entity) {
        return TEXTURE;
    }
}