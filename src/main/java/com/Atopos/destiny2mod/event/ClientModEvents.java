package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.client.gui.DiagnosticScreen;
import com.Atopos.destiny2mod.client.model.SolarFlareModel;
import com.Atopos.destiny2mod.client.renderer.entity.SolarFlareRenderer;
import com.Atopos.destiny2mod.client.renderer.entity.WellOfRadianceRenderer;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.KeyInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import com.Atopos.destiny2mod.network.packet.PacketWellOfRadiance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件处理器类
 * <p>
 * 专门处理客户端相关的事件，包括：
 * 1. 实体渲染器注册
 * 2. 图层定义注册
 * 3. 按键映射注册
 * 4. 按键输入事件处理
 * </p>
 * 修复：解决了之前被错误合并到 DestinyHudOverlay.java 的问题
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    /**
     * 注册实体渲染器
     * <p>
     * 在这个方法中，为所有自定义实体注册对应的渲染器
     * 每个实体类型都需要一个渲染器来确定如何在客户端显示
     * </p>
     * 
     * @param event 实体渲染器注册事件，提供注册渲染器的方法
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册完美逆行投射物渲染器，使用默认的投掷物品渲染器
        event.registerEntityRenderer(EntityInit.PERFECT_RETROGRADE_PROJECTILE.get(), ThrownItemRenderer::new);
        // 注册太阳能手榴弹投射物渲染器，使用默认的投掷物品渲染器
        event.registerEntityRenderer(EntityInit.SOLAR_GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
        // 注册太阳能火焰实体渲染器，使用自定义的 SolarFlareRenderer
        event.registerEntityRenderer(EntityInit.SOLAR_FLARE_ENTITY.get(), SolarFlareRenderer::new);
        // 注册太阳能响指投射物渲染器，使用默认的投掷物品渲染器
        event.registerEntityRenderer(EntityInit.SOLAR_SNAP_PROJECTILE.get(), ThrownItemRenderer::new);
        // 注册强能裂隙实体渲染器，使用自定义的 WellOfRadianceRenderer
        event.registerEntityRenderer(EntityInit.WELL_OF_RADIANCE_ENTITY.get(), WellOfRadianceRenderer::new);
    }

    /**
     * 注册图层定义
     * <p>
     * 为实体模型注册图层定义，用于渲染实体的纹理和模型
     * 目前主要为 SolarFlareModel 注册图层定义
     * </p>
     * 
     * @param event 图层定义注册事件，提供注册图层定义的方法
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册太阳能火焰模型的图层定义
        event.registerLayerDefinition(SolarFlareModel.LAYER_LOCATION, SolarFlareModel::createBodyLayer);
    }

    /**
     * 注册按键映射
     * <p>
     * 在这个方法中，将所有自定义的按键映射注册到游戏中
     * 注册后的按键才能被游戏识别和响应
     * </p>
     * 
     * @param event 按键映射注册事件，提供注册按键映射的方法
     */
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        // 注册手榴弹按键映射
        event.register(KeyInit.GRENADE_KEY);
        // 注册近战（响指）按键映射
        event.register(KeyInit.MELEE_KEY);
        // 注册诊断终端按键映射
        event.register(KeyInit.DIAG_KEY);
        // 注册强能裂隙技能按键映射
        event.register(KeyInit.WELL_OF_RADIANCE_KEY);
    }

    /**
     * 客户端Forge事件内部类
     * <p>
     * 用于处理Forge客户端事件，主要是按键输入事件
     * 这个内部类使用不同的事件总线 (Forge总线)，用于处理运行时事件
     * </p>
     */
    @Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        
        /**
         * 处理按键输入事件
         * <p>
         * 当玩家按下注册的按键时，触发相应的技能效果
         * 检查按键是否被按下，并发送相应的网络数据包到服务器
         * </p>
         * 
         * @param event 按键输入事件，包含按键的状态信息
         */
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            // 获取Minecraft实例
            Minecraft mc = Minecraft.getInstance();
            // 如果玩家为null，直接返回（例如在主菜单中）
            if (mc.player == null) return;

            // F10：打开诊断终端界面
            if (KeyInit.DIAG_KEY.consumeClick()) {
                mc.setScreen(new DiagnosticScreen());
            }

            // G键：投掷手榴弹
            if (KeyInit.GRENADE_KEY.consumeClick()) {
                // 检查手榴弹是否在冷却中
                if (!mc.player.getCooldowns().isOnCooldown(ItemInit.SOLAR_GRENADE.get())) {
                    // 发送手榴弹投掷数据包到服务器
                    PacketHandler.INSTANCE.sendToServer(new PacketThrowGrenade());
                    
                    // 客户端预判冷却：立即应用冷却以更新UI和防止连点
                    boolean isOverloaded = mc.player.getPersistentData().getBoolean("OverloadActive");
                    // 冷却调整：普通 40秒 (800 ticks)，超载 0.8秒 (16 ticks)
                    int duration = isOverloaded ? 16 : 800;
                    mc.player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), duration);
                }
            }

            // C键：太阳能响指
            if (KeyInit.MELEE_KEY.consumeClick()) {
                // 检查响指是否在冷却中
                if (!mc.player.getCooldowns().isOnCooldown(ItemInit.GHOST_GENERAL.get())) {
                    // 发送太阳能响指数据包到服务器
                    PacketHandler.INSTANCE.sendToServer(new PacketSolarSnap());
                    
                    // 客户端预判冷却
                    boolean isOverloaded = mc.player.getPersistentData().getBoolean("OverloadActive");
                    // 冷却调整：普通 25秒 (500 ticks)，超载 0.8秒 (16 ticks)
                    int duration = isOverloaded ? 16 : 500;
                    mc.player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), duration);
                }
            }
            
            // V键：强能裂隙技能
            if (KeyInit.WELL_OF_RADIANCE_KEY.consumeClick()) {
                // 检查强能裂隙是否在冷却中
                if (!mc.player.getCooldowns().isOnCooldown(ItemInit.WELL_OF_RADIANCE.get())) {
                // 发送强能裂隙数据包到服务器
                PacketHandler.INSTANCE.sendToServer(new PacketWellOfRadiance());

                // 客户端预判冷却
                    boolean isOverloaded = mc.player.getPersistentData().getBoolean("OverloadActive");
                    // 冷却时间调整：普通 60秒 (1200 ticks)，超载 1.2秒 (24 ticks)
                    int duration = isOverloaded ? 24 : 1200;
                    mc.player.getCooldowns().addCooldown(ItemInit.WELL_OF_RADIANCE.get(), duration);
                }
            }
        }
    }
}