package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.client.gui.DiagnosticScreen;
import com.Atopos.destiny2mod.client.model.SolarFlareModel;
import com.Atopos.destiny2mod.client.renderer.entity.SolarFlareRenderer;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.KeyInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件处理器 (独立文件)
 * 修复：解决了之前被错误合并到 DestinyHudOverlay.java 的问题
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.PERFECT_RETROGRADE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_FLARE_ENTITY.get(), SolarFlareRenderer::new);
        event.registerEntityRenderer(EntityInit.SOLAR_SNAP_PROJECTILE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SolarFlareModel.LAYER_LOCATION, SolarFlareModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.GRENADE_KEY);
        event.register(KeyInit.MELEE_KEY);
        event.register(KeyInit.DIAG_KEY);
    }

    @Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // F10 诊断终端
            if (KeyInit.DIAG_KEY.consumeClick()) {
                mc.setScreen(new DiagnosticScreen());
            }

            // G 手雷
            if (KeyInit.GRENADE_KEY.consumeClick()) {
                if (!mc.player.getCooldowns().isOnCooldown(ItemInit.SOLAR_GRENADE.get())) {
                    PacketHandler.INSTANCE.sendToServer(new PacketThrowGrenade());
                }
            }

            // C 响指
            if (KeyInit.MELEE_KEY.consumeClick()) {
                if (!mc.player.getCooldowns().isOnCooldown(ItemInit.GHOST_GENERAL.get())) {
                    PacketHandler.INSTANCE.sendToServer(new PacketSolarSnap());
                }
            }
        }
    }
}