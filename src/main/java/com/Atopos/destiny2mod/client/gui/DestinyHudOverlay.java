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
 * Destiny 2 核心 HUD 渲染器
 * 适配 854x480 精准布局 (V14 全息投影左移版)
 * 修复：将 UI 整体左移以避免与物品栏重叠
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DestinyHudOverlay {

    private static final float BASE_W = 854.0f;
    private static final float BASE_H = 480.0f;

    // --- 全息动效状态变量 ---
    private static float lastYaw = 0;
    private static float lastPitch = 0;
    private static float swayX = 0;
    private static float swayY = 0;
    private static float jumpOffset = 0;

    private static final ResourceLocation GRENADE_ICON = new ResourceLocation(Destiny2Mod.MODID, "textures/item/solar_grenade.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation SUPER_ICON = new ResourceLocation("minecraft", "textures/item/nether_star.png");
    private static final ResourceLocation CLASS_ICON = new ResourceLocation("minecraft", "textures/item/barrier.png");

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "destiny_hud_main", HUD_RENDERER);
    }

    public static final IGuiOverlay HUD_RENDERER = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.player.isSpectator()) return;

        Player player = mc.player;

        // --- 全息动效计算 ---
        updateHolographicSway(player, partialTick);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // 应用全息平移偏移
        float breathe = Mth.sin((mc.level.getGameTime() + partialTick) * 0.05f) * 0.5f;
        poseStack.translate(swayX, swayY + jumpOffset + breathe, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 1. 渲染左侧 Buff 列表
        renderLeftBuffs(guiGraphics, mc, player, screenWidth, screenHeight);

        // 2. 渲染技能面板 (已应用左移坐标)
        renderSkillPanel(guiGraphics, mc, player, screenWidth, screenHeight, partialTick);

        RenderSystem.disableBlend();
        poseStack.popPose();
    };

    private static void updateHolographicSway(Player player, float partialTick) {
        float currentYaw = player.getViewYRot(partialTick);
        float currentPitch = player.getViewXRot(partialTick);

        float deltaYaw = lastYaw - currentYaw;
        float deltaPitch = lastPitch - currentPitch;
        float sensitivity = 0.8f;

        float targetSwayX = Mth.clamp(deltaYaw * sensitivity, -5.0f, 5.0f);
        float targetSwayY = Mth.clamp(deltaPitch * sensitivity, -5.0f, 5.0f);

        swayX = Mth.lerp(0.1f, swayX, targetSwayX);
        swayY = Mth.lerp(0.1f, swayY, targetSwayY);

        lastYaw = currentYaw;
        lastPitch = currentPitch;

        float verticalMotion = (float) player.getDeltaMovement().y;
        if (!player.onGround()) {
            jumpOffset = Mth.lerp(0.05f, jumpOffset, verticalMotion * -10.0f);
        } else {
            jumpOffset = Mth.lerp(0.2f, jumpOffset, 0);
        }
    }

    private static void renderSkillPanel(GuiGraphics graphics, Minecraft mc, Player player, int sw, int sh, float pt) {
        float scalex = sw / BASE_W;
        float scaley = sh / BASE_H;
        int lineColor = 0xCCFFFFFF;

        // --- [坐标修正] 整体向左平移以避开物品栏 ---
        // 原中心点 80 -> 现中心点 60
        int centerX = (int)(60 * scalex);
        int centerY = (int)(364 * scaley);
        int lineLength = (int)(64 * scalex);
        int lineThickness = (int)(2 * scaley);

        // 绘制交叉支架
        drawCenteredRotatedLine(graphics, centerX, centerY, lineLength, lineThickness, 45, lineColor);
        drawCenteredRotatedLine(graphics, centerX, centerY, lineLength, lineThickness, -45, lineColor);

        // 横向线起点也相应左移 (105 -> 85)
        graphics.fill((int)(85 * scalex), (int)(364 * scaley), (int)(230 * scalex), (int)((364 + 2) * scaley), lineColor);

        // --- 技能槽位渲染 (坐标已同步左移) ---

        // 1. 大招槽位 (x: 54 -> 34)
        renderAbilitySlot(graphics, mc, player, (int)(34 * scalex), (int)(338 * scaley), (int)(52 * scalex), (int)(52 * scaley),
                45, SUPER_ICON, null, 1000, pt, "");

        // 2. 近战 [C] (x: 130 -> 110)
        renderAbilitySlot(graphics, mc, player, (int)(110 * scalex), (int)(388 * scaley), (int)(34 * scalex), (int)(34 * scaley),
                0, MELEE_ICON, ItemInit.GHOST_GENERAL.get(), 100, pt, "C");

        // 3. 手雷 [G] (x: 172 -> 152)
        renderAbilitySlot(graphics, mc, player, (int)(152 * scalex), (int)(388 * scaley), (int)(34 * scalex), (int)(34 * scaley),
                0, GRENADE_ICON, ItemInit.SOLAR_GRENADE.get(), 200, pt, "G");

        // 4. 职业技能 [V] (x: 214 -> 194)
        renderAbilitySlot(graphics, mc, player, (int)(194 * scalex), (int)(388 * scaley), (int)(34 * scalex), (int)(34 * scaley),
                0, CLASS_ICON, null, 400, pt, "V");
    }

    private static void drawCenteredRotatedLine(GuiGraphics g, int cx, int cy, int length, int thickness, float degrees, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(degrees)));
        pose.translate(-length / 2f, -thickness / 2f, 0);
        g.fill(0, 0, length, thickness, color);
        pose.popPose();
    }

    private static void renderAbilitySlot(GuiGraphics g, Minecraft mc, Player player, int x, int y, int w, int h, float rotation, ResourceLocation icon, net.minecraft.world.item.Item item, int maxCD, float pt, String keyHint) {
        float cd = (item != null) ? player.getCooldowns().getCooldownPercent(item, pt) : 0;

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x + w / 2f, y + h / 2f, 0);
        if (rotation != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(rotation)));
        pose.translate(-w / 2f, -h / 2f, 0);

        g.fill(0, 0, w, h, 0x60101010);
        g.renderOutline(-1, -1, w + 2, h + 2, 0xAAFFFFFF);

        pose.pushPose();
        pose.translate(w / 2f, h / 2f, 0);
        if (rotation != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rotation)));
        pose.translate(-w / 2f, -h / 2f, 0);

        RenderSystem.setShaderTexture(0, icon);
        if (cd > 0) RenderSystem.setShaderColor(0.2f, 0.4f, 0.6f, 0.8f);
        else RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int iconSize = (int)(w * 0.65f);
        g.blit(icon, (w - iconSize) / 2, (h - iconSize) / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        pose.popPose();

        if (cd > 0) {
            int coolH = (int)(h * cd);
            g.fill(0, h - coolH, w, h, 0xA0000000);
            String sec = String.valueOf(((int)(maxCD * cd) / 20) + 1);
            pose.pushPose();
            pose.translate(w/2f, h/2f, 0);
            if (rotation != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rotation)));
            g.drawCenteredString(mc.font, sec, 0, -4, 0xAAFFFFFF);
            pose.popPose();
        }

        if (!keyHint.isEmpty()) {
            pose.pushPose();
            pose.scale(0.45f, 0.45f, 0.45f);
            g.drawString(mc.font, "[" + keyHint + "]", 4, 4, 0xBBBBBB);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void renderLeftBuffs(GuiGraphics graphics, Minecraft mc, Player player, int sw, int sh) {
        CompoundTag nbt = player.getPersistentData();
        List<String> list = new ArrayList<>();
        if (nbt.getBoolean("OverloadActive")) {
            list.add(String.format("§d§l超载 §f%.1fs", nbt.getInt("OverloadBuffTimer") / 20.0f));
        } else if (nbt.getInt("OverloadKillWindow") > 0) {
            list.add(String.format("§e准备超载 %d/5 §7(%ds)", nbt.getInt("OverloadKillCount"), nbt.getInt("OverloadKillWindow") / 20));
        }

        int y = sh / 2 - 40;
        for (String s : list) {
            int textW = mc.font.width(s);
            graphics.fill(10, y - 2, 14 + textW, y + 10, 0x80000000);
            graphics.drawString(mc.font, s, 12, y, 0xFFFFFF, true);
            y += 14;
        }
    }
}