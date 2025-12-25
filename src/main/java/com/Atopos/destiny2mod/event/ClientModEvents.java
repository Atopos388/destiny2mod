package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.client.model.PerfectRetrogradeModel;
import com.Atopos.destiny2mod.client.model.SolarFlareModel;
import com.Atopos.destiny2mod.client.renderer.entity.PerfectRetrogradeRenderer;
import com.Atopos.destiny2mod.client.renderer.entity.SolarFlareRenderer;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.KeyInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import com.Atopos.destiny2mod.util.IgniteHandler;
import com.mojang.blaze3d.systems.RenderSystem; // [新增] 导入渲染系统
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.PERFECT_RETROGRADE_PROJECTILE.get(), PerfectRetrogradeRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_FLARE_ENTITY.get(), SolarFlareRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_SNAP_PROJECTILE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SolarFlareModel.LAYER_LOCATION, SolarFlareModel::createBodyLayer);
        event.registerLayerDefinition(PerfectRetrogradeModel.LAYER_LOCATION, PerfectRetrogradeModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.GRENADE_KEY);
        event.register(KeyInit.MELEE_KEY);
    }

    @Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        // [新增/修改] 渲染事件监听：实现点燃时的红色效果
        @SubscribeEvent
        public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
            // 检查实体 NBT 数据
            int fuse = event.getEntity().getPersistentData().getInt(IgniteHandler.FUSE_KEY);

            // 如果正在延迟引燃 (1秒倒计时中)
            if (fuse > 0) {
                // 强制将实体渲染为红色
                // 这里使用 RenderSystem 设置颜色倍增器 (R=1.0, G=0.3, B=0.3, A=1.0)
                // 这会让实体看起来整体泛红，类似于受伤但更持久
                RenderSystem.setShaderColor(1.0F, 0.3F, 0.3F, 1.0F);
            }
        }

        // [新增] 渲染后重置颜色，防止染红其他实体
        @SubscribeEvent
        public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            if (KeyInit.GRENADE_KEY.consumeClick()) {
                if (!player.getCooldowns().isOnCooldown(ItemInit.SOLAR_GRENADE.get())) {
                    PacketHandler.INSTANCE.sendToServer(new PacketThrowGrenade());
                }
            }

            if (KeyInit.MELEE_KEY.consumeClick()) {
                if (!player.getCooldowns().isOnCooldown(ItemInit.GHOST_GENERAL.get())) {
                    PacketHandler.INSTANCE.sendToServer(new PacketSolarSnap());
                }
            }
        }
    }
}