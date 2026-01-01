package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * 命运HUD覆盖层类
 * <p>
 * 负责在游戏界面上渲染自定义的HUD元素，包括：
 * 1. 技能面板（手榴弹、近战、强能裂隙等）
 * 2. 左侧Buff信息显示
 * 3. 全息摇摆效果
 * 4. 技能冷却时间
 * </p>
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DestinyHudOverlay {

    /**
     * 基础宽度，用于UI缩放计算
     * <p>
     * 所有UI元素的位置和大小都基于这个基础宽度进行缩放
     * 确保在不同分辨率下UI元素保持正确的比例
     * </p>
     */
    private static final float BASE_W = 854.0f;
    
    /**
     * 基础高度，用于UI缩放计算
     * <p>
     * 所有UI元素的位置和大小都基于这个基础高度进行缩放
     * 确保在不同分辨率下UI元素保持正确的比例
     * </p>
     */
    private static final float BASE_H = 480.0f;
    
    /**
     * 用于计算全息摇摆效果的变量
     * <p>
     * lastYaw: 上一帧的偏航角度
     * lastPitch: 上一帧的俯仰角度
     * swayX: X轴摇摆偏移量
     * swayY: Y轴摇摆偏移量
     * jumpOffset: 跳跃偏移量
     * </p>
     */
    private static float lastYaw = 0, lastPitch = 0, swayX = 0, swayY = 0, jumpOffset = 0;

    /**
     * 技能图标资源定位器
     * <p>
     * GRENADE_ICON: 手榴弹图标
     * MELEE_ICON: 近战图标
     * SUPER_ICON: 超级技能图标
     * CLASS_ICON: 职业技能图标（用于强能裂隙）
     * </p>
     */
    private static final ResourceLocation GRENADE_ICON = new ResourceLocation(Destiny2Mod.MODID, "textures/item/solar_grenade.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation SUPER_ICON = new ResourceLocation("minecraft", "textures/item/nether_star.png");
    private static final ResourceLocation CLASS_ICON = new ResourceLocation("minecraft", "textures/item/barrier.png");

    /**
     * 注册HUD覆盖层
     * <p>
     * 将自定义的HUD渲染器注册到游戏中
     * 使其显示在原版HOTBAR之上
     * </p>
     * 
     * @param event 覆盖层注册事件，提供注册覆盖层的方法
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // 注册HUD覆盖层，显示在原版HOTBAR之上
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "destiny_hud_main", HUD_RENDERER);
    }

    /**
     * 主要HUD渲染器
     * <p>
     * 负责渲染所有HUD元素，包括：
     * 1. 更新全息摇摆效果
     * 2. 渲染左侧Buff信息
     * 3. 渲染技能面板
     * </p>
     */
    public static final IGuiOverlay HUD_RENDERER = (gui, graphics, pt, sw, sh) -> {
        // 获取Minecraft实例
        Minecraft mc = Minecraft.getInstance();
        // 如果隐藏GUI或玩家为null，直接返回
        if (mc.options.hideGui || mc.player == null) return;
        Player player = mc.player;

        // 更新全息摇摆效果
        updateHolographicSway(player, pt);

        // 1. 渲染超载系统UI（底部居中）
        renderOverloadHud(graphics, mc, player, sw, sh);

        // 2. 渲染技能面板（全息效果）
        PoseStack pose = graphics.pose();
        pose.pushPose();
        // 添加呼吸效果（每20游戏刻一个周期）
        float breathe = Mth.sin((mc.level.getGameTime() + pt) * 0.05f) * 0.5f;
        // 应用摇摆、跳跃和呼吸偏移
        pose.translate(swayX, swayY + jumpOffset + breathe, 0);

        // 启用混合模式，使UI元素有半透明效果
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 渲染技能面板
        renderSkillPanel(graphics, mc, player, sw, sh, pt);

        // 禁用混合模式
        RenderSystem.disableBlend();
        pose.popPose();
    };

    /**
     * 更新全息摇摆效果
     * <p>
     * 根据玩家的视角移动计算摇摆偏移量
     * 使技能面板产生跟随视角的全息摇摆效果
     * 同时处理跳跃偏移
     * </p>
     * 
     * @param player 玩家实体
     * @param pt 部分刻进度（0.0-1.0）
     */
    private static void updateHolographicSway(Player player, float pt) {
        // 计算偏航和俯仰的变化量
        float dy = lastYaw - player.getViewYRot(pt);
        float dp = lastPitch - player.getViewXRot(pt);
        
        // 更新摇摆偏移量，使用平滑插值和限制范围
        swayX = Mth.lerp(0.1f, swayX, Mth.clamp(dy * 0.8f, -5f, 5f));
        swayY = Mth.lerp(0.1f, swayY, Mth.clamp(dp * 0.8f, -5f, 5f));
        
        // 更新上一帧的角度值
        lastYaw = player.getViewYRot(pt); 
        lastPitch = player.getViewXRot(pt);
        
        // 处理跳跃偏移：如果在地面上，平滑恢复到0；否则根据垂直速度计算偏移
        jumpOffset = player.onGround() ? 
            Mth.lerp(0.2f, jumpOffset, 0) : 
            Mth.lerp(0.05f, jumpOffset, (float)player.getDeltaMovement().y * -10f);
    }

    /**
     * 渲染技能面板
     * <p>
     * 渲染技能面板的背景线条和各个技能槽
     * 包括：
     * 1. 绘制旋转线条作为背景装饰
     * 2. 绘制水平中心线
     * 3. 渲染各个技能槽（超级技能、近战、手榴弹、强能裂隙）
     * </p>
     * 
     * @param g GuiGraphics对象，用于绘制HUD元素
     * @param mc Minecraft实例
     * @param player 玩家实体
     * @param sw 屏幕宽度
     * @param sh 屏幕高度
     * @param pt 部分刻进度（0.0-1.0）
     */
    private static void renderSkillPanel(GuiGraphics g, Minecraft mc, Player player, int sw, int sh, float pt) {
        // 计算缩放因子：基于基础分辨率的缩放
        float sx = sw / BASE_W, sy = sh / BASE_H;
        // UI元素颜色（半透明白色）
        int color = 0xCCFFFFFF;

        // V14坐标系：中心位置
        int cx = (int)(60 * sx), cy = (int)(364 * sy);
        // 线条长度和厚度
        int l = (int)(64 * sx), t = (int)(2 * sy);

        // 绘制45度和-45度的旋转线条
        drawRotatedLine(g, cx, cy, l, t, 45, color);
        drawRotatedLine(g, cx, cy, l, t, -45, color);
        // 绘制水平中心线
        g.fill((int)(85 * sx), (int)(364 * sy), (int)(230 * sx), (int)((364 + 2) * sy), color);

        // 渲染各个技能槽
        // 超级技能槽（右上角，旋转45度）
        renderSlot(g, mc, player, (int)(34*sx), (int)(338*sy), (int)(52*sx), (int)(52*sy), 45, SUPER_ICON, null, 1000, pt, "");
        
        // 获取超载状态
        boolean isOverloaded = player.getPersistentData().getBoolean("OverloadActive");
        
        // 动态设置最大冷却时间
        // 近战（响指）：正常 500 (25s)，超载 16 (0.8s)
        int meleeMax = isOverloaded ? 16 : 500;
        // 手榴弹（太阳能手雷）：正常 800 (40s)，超载 16 (0.8s)
        int grenadeMax = isOverloaded ? 16 : 800;
        // 职业技能（强能裂隙）：正常 1200 (60s)，超载 24 (1.2s)
        int classMax = isOverloaded ? 24 : 1200;

        // 近战技能槽（C键）
        renderSlot(g, mc, player, (int)(110*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, MELEE_ICON, ItemInit.GHOST_GENERAL.get(), meleeMax, pt, "C");
        // 手榴弹技能槽（G键）
        renderSlot(g, mc, player, (int)(152*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, GRENADE_ICON, ItemInit.SOLAR_GRENADE.get(), grenadeMax, pt, "G");
        // 强能裂隙技能槽（V键）
        renderSlot(g, mc, player, (int)(194*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, CLASS_ICON, ItemInit.WELL_OF_RADIANCE.get(), classMax, pt, "V");
    }

    /**
     * 渲染技能槽
     * <p>
     * 渲染单个技能槽，包括：
     * 1. 背景和边框
     * 2. 技能图标
     * 3. 冷却时间显示
     * 4. 按键提示
     * </p>
     * 
     * @param g GuiGraphics对象，用于绘制HUD元素
     * @param mc Minecraft实例
     * @param player 玩家实体
     * @param x 技能槽X坐标
     * @param y 技能槽Y坐标
     * @param w 技能槽宽度
     * @param h 技能槽高度
     * @param rot 旋转角度（度）
     * @param icon 技能图标资源定位器
     * @param item 关联的物品（用于冷却时间计算）
     * @param max 最大冷却时间（游戏刻）
     * @param pt 部分刻进度（0.0-1.0）
     * @param key 按键提示文本
     */
    private static void renderSlot(GuiGraphics g, Minecraft mc, Player player, int x, int y, int w, int h, float rot, ResourceLocation icon, net.minecraft.world.item.Item item, int max, float pt, String key) {
        // 计算冷却时间百分比
        float cd = (item != null) ? player.getCooldowns().getCooldownPercent(item, pt) : 0;
        PoseStack pose = g.pose();
        
        // 开始新的渲染层
        pose.pushPose();
        // 平移到技能槽中心
        pose.translate(x + w/2f, y + h/2f, 0);
        // 应用旋转（如果有）
        if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(rot)));
        // 平移回左上角
        pose.translate(-w/2f, -h/2f, 0);

        // 绘制技能槽背景（半透明深灰色）
        g.fill(0, 0, w, h, 0x60101010);
        // 绘制技能槽边框（半透明白色）
        g.renderOutline(-1, -1, w+2, h+2, 0xAAFFFFFF);

        // 开始图标渲染层
        pose.pushPose();
        // 平移回中心，用于取消旋转
        pose.translate(w/2f, h/2f, 0);
        // 取消之前的旋转，确保图标正常显示
        if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rot)));
        // 平移回左上角
        pose.translate(-w/2f, -h/2f, 0);

        // 设置技能图标纹理
        RenderSystem.setShaderTexture(0, icon);
        // 如果在冷却中，降低图标亮度
        if (cd > 0) RenderSystem.setShaderColor(0.2f, 0.4f, 0.6f, 0.8f);
        else RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        // 计算图标大小（技能槽宽度的65%）
        int s = (int)(w * 0.65f);
        // 绘制技能图标，居中显示
        g.blit(icon, (w-s)/2, (h-s)/2, 0, 0, s, s, s, s);
        // 恢复默认颜色
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        
        // 结束图标渲染层
        pose.popPose();

        // 如果在冷却中，渲染冷却覆盖层和剩余时间
        if (cd > 0) {
            // 计算冷却覆盖层高度
            int ch = (int)(h * cd);
            // 绘制冷却覆盖层（半透明黑色）
            g.fill(0, h-ch, w, h, 0xA0000000);
            // 计算剩余冷却时间（秒），向上取整
            String t = "" + Mth.ceil(max * cd / 20.0f);
            
            // 开始文本渲染层
            pose.pushPose();
            // 平移到中心，用于取消旋转
            pose.translate(w/2f, h/2f, 0);
            // 取消之前的旋转，确保文本正常显示
            if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rot)));
            // 绘制中心文本（剩余冷却时间）
            g.drawCenteredString(mc.font, t, 0, -4, 0xAAFFFFFF);
            // 结束文本渲染层
            pose.popPose();
        }

        // 如果有按键提示，渲染按键文本
        if (!key.isEmpty()) {
            // 开始按键文本渲染层，缩小文本
            pose.pushPose(); 
            pose.scale(0.45f, 0.45f, 0.45f);
            // 绘制按键提示（例如：[V]）
            g.drawString(mc.font, "["+key+"]", 4, 4, 0xBBBBBB);
            // 结束按键文本渲染层
            pose.popPose();
        }
        
        // 结束渲染层
        pose.popPose();
    }

    /**
     * 绘制旋转线条
     * <p>
     * 绘制一条指定长度、厚度、角度和颜色的旋转线条
     * 用于技能面板的背景装饰
     * </p>
     * 
     * @param g GuiGraphics对象，用于绘制线条
     * @param cx 中心X坐标
     * @param cy 中心Y坐标
     * @param l 线条长度
     * @param t 线条厚度
     * @param deg 旋转角度（度）
     * @param color 线条颜色
     */
    private static void drawRotatedLine(GuiGraphics g, int cx, int cy, int l, int t, float deg, int color) {
        PoseStack pose = g.pose();
        // 开始新的渲染层
        pose.pushPose();
        // 平移到中心位置
        pose.translate(cx, cy, 0);
        // 应用旋转
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(deg)));
        // 平移到线条左上角
        pose.translate(-l/2f, -t/2f, 0);
        // 绘制线条
        g.fill(0, 0, l, t, color);
        // 结束渲染层
        pose.popPose();
    }

    /**
     * 绘制旋转正方形（实心）
     */
    private static void drawRotatedSquare(GuiGraphics g, int x, int y, int size, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(45)));
        pose.translate(-size/2f, -size/2f, 0);
        g.fill(0, 0, size, size, color);
        pose.popPose();
    }

    /**
     * 绘制旋转正方形（边框）
     */
    private static void drawRotatedSquareOutline(GuiGraphics g, int x, int y, int size, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(45)));
        pose.translate(-size/2f, -size/2f, 0);
        g.renderOutline(0, 0, size, size, color);
        pose.popPose();
    }

    /**
     * 渲染超载系统HUD
     * <p>
     * 在屏幕下方中间（血条上方）渲染玩家的超载信息，包括：
     * 1. 超载状态 (紫色菱形UI)
     * 2. 超载准备状态 (金色菱形进度条)
     * </p>
     * 
     * @param g GuiGraphics对象，用于绘制Buff信息
     * @param mc Minecraft实例
     * @param player 玩家实体
     * @param sw 屏幕宽度
     * @param sh 屏幕高度
     */
    private static void renderOverloadHud(GuiGraphics g, Minecraft mc, Player player, int sw, int sh) {
        CompoundTag nbt = player.getPersistentData();
        
        // 基础缩放因子
        float sx = sw / BASE_W;
        float sy = sh / BASE_H;
        
        // UI中心位置：屏幕底部中间，位于血条/饱食度条上方
        int cx = sw / 2;
        int cy = sh - (int)(120 * sy); 

        // 1. 超载激活状态 (紫色主题)
        if (nbt.getBoolean("OverloadActive")) {
            int timer = nbt.getInt("OverloadBuffTimer");
            float progress = timer / 300.0f; // 15秒总时长
            
            // 绘制主菱形背景
            int size = (int)(40 * sx);
            drawRotatedSquare(g, cx, cy, size, 0x802A002A); // 深紫背景
            drawRotatedSquareOutline(g, cx, cy, size, 0xFFAA00AA); // 亮紫边框
            
            // 绘制中心剩余时间
            String text = String.format("%.1f", timer / 20.0f);
            poseCenteredString(g, mc.font, text, cx, cy, 0xFFFFAAFF);
            
            // 绘制下方标题
            poseCenteredString(g, mc.font, "超 载", cx, cy + (int)(30*sy), 0xFFD400D4);
            
            // 绘制底部进度条
            int barW = (int)(60 * sx);
            int barH = (int)(3 * sy);
            int barX = cx - barW/2;
            int barY = cy + (int)(40 * sy);
            
            g.fill(barX, barY, barX + barW, barY + barH, 0x80000000); // 条背景
            g.fill(barX, barY, barX + (int)(barW * progress), barY + barH, 0xFFD400D4); // 条前景
        } 
        // 2. 超载准备状态 (金色主题)
        else if (nbt.getInt("OverloadKillWindow") > 0) {
            int kills = nbt.getInt("OverloadKillCount");
            int window = nbt.getInt("OverloadKillWindow");
            
            // 绘制5个小菱形作为击杀计数器
            int gap = (int)(18 * sx);
            int startX = cx - (2 * gap); 
            
            for(int i=0; i<5; i++) {
                int dx = startX + i * gap;
                int dSize = (int)(12 * sx);
                // 已击杀显示亮金，未击杀显示暗金
                int color = (i < kills) ? 0xFFFFD700 : 0x40FFD700; 
                int outline = (i < kills) ? 0xFFFFFFFF : 0x60FFD700;
                
                // 绘制小菱形
                drawRotatedSquare(g, dx, cy, dSize, 0x80202000); // 背景
                if (i < kills) {
                    drawRotatedSquare(g, dx, cy, (int)(dSize*0.6), color); // 填充核心
                }
                drawRotatedSquareOutline(g, dx, cy, dSize, outline); // 边框
            }
            
            // 绘制下方标题
            poseCenteredString(g, mc.font, "充 能", cx, cy + (int)(20*sy), 0xFFFFD700);
            
            // 绘制底部倒计时条
            int barW = (int)(80 * sx);
            int barH = (int)(3 * sy);
            int barX = cx - barW/2;
            int barY = cy + (int)(30 * sy);
            
            g.fill(barX, barY, barX + barW, barY + barH, 0x80000000);
            g.fill(barX, barY, barX + (int)(barW * (window / 200.0f)), barY + barH, 0xFFFFD700);
        }
    }
    
    /**
     * 辅助方法：绘制居中且带缩放修正的文本
     */
    private static void poseCenteredString(GuiGraphics g, net.minecraft.client.gui.Font font, String text, int x, int y, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        // 稍微缩小字体以适应UI
        float scale = 0.8f;
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1f);
        g.drawCenteredString(font, text, 0, -4, color);
        pose.popPose();
    }
}