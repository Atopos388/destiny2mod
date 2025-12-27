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

@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DestinyHudOverlay {

    private static final float BASE_W = 854.0f;
    private static final float BASE_H = 480.0f;
    private static float lastYaw = 0, lastPitch = 0, swayX = 0, swayY = 0, jumpOffset = 0;

    private static final ResourceLocation GRENADE_ICON = new ResourceLocation(Destiny2Mod.MODID, "textures/item/solar_grenade.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation SUPER_ICON = new ResourceLocation("minecraft", "textures/item/nether_star.png");
    private static final ResourceLocation CLASS_ICON = new ResourceLocation("minecraft", "textures/item/barrier.png");

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "destiny_hud_main", HUD_RENDERER);
    }

    public static final IGuiOverlay HUD_RENDERER = (gui, graphics, pt, sw, sh) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        Player player = mc.player;

        updateHolographicSway(player, pt);

        // 1. 左侧 Buff (固定)
        renderLeftBuffs(graphics, mc, player, sw, sh);

        // 2. 技能面板 (全息)
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float breathe = Mth.sin((mc.level.getGameTime() + pt) * 0.05f) * 0.5f;
        pose.translate(swayX, swayY + jumpOffset + breathe, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        renderSkillPanel(graphics, mc, player, sw, sh, pt);

        RenderSystem.disableBlend();
        pose.popPose();
    };

    private static void updateHolographicSway(Player player, float pt) {
        float dy = lastYaw - player.getViewYRot(pt);
        float dp = lastPitch - player.getViewXRot(pt);
        swayX = Mth.lerp(0.1f, swayX, Mth.clamp(dy * 0.8f, -5f, 5f));
        swayY = Mth.lerp(0.1f, swayY, Mth.clamp(dp * 0.8f, -5f, 5f));
        lastYaw = player.getViewYRot(pt); lastPitch = player.getViewXRot(pt);
        jumpOffset = player.onGround() ? Mth.lerp(0.2f, jumpOffset, 0) : Mth.lerp(0.05f, jumpOffset, (float)player.getDeltaMovement().y * -10f);
    }

    private static void renderSkillPanel(GuiGraphics g, Minecraft mc, Player player, int sw, int sh, float pt) {
        float sx = sw / BASE_W, sy = sh / BASE_H;
        int color = 0xCCFFFFFF;

        // V14 坐标系：中心 60
        int cx = (int)(60 * sx), cy = (int)(364 * sy);
        int l = (int)(64 * sx), t = (int)(2 * sy);

        drawRotatedLine(g, cx, cy, l, t, 45, color);
        drawRotatedLine(g, cx, cy, l, t, -45, color);
        g.fill((int)(85 * sx), (int)(364 * sy), (int)(230 * sx), (int)((364 + 2) * sy), color);

        renderSlot(g, mc, player, (int)(34*sx), (int)(338*sy), (int)(52*sx), (int)(52*sy), 45, SUPER_ICON, null, 1000, pt, "");
        renderSlot(g, mc, player, (int)(110*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, MELEE_ICON, ItemInit.GHOST_GENERAL.get(), 100, pt, "C");
        renderSlot(g, mc, player, (int)(152*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, GRENADE_ICON, ItemInit.SOLAR_GRENADE.get(), 200, pt, "G");
        renderSlot(g, mc, player, (int)(194*sx), (int)(388*sy), (int)(34*sx), (int)(34*sy), 0, CLASS_ICON, null, 400, pt, "V");
    }

    private static void renderSlot(GuiGraphics g, Minecraft mc, Player player, int x, int y, int w, int h, float rot, ResourceLocation icon, net.minecraft.world.item.Item item, int max, float pt, String key) {
        float cd = (item != null) ? player.getCooldowns().getCooldownPercent(item, pt) : 0;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x + w/2f, y + h/2f, 0);
        if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(rot)));
        pose.translate(-w/2f, -h/2f, 0);

        g.fill(0, 0, w, h, 0x60101010);
        g.renderOutline(-1, -1, w+2, h+2, 0xAAFFFFFF);

        pose.pushPose();
        pose.translate(w/2f, h/2f, 0);
        if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rot)));
        pose.translate(-w/2f, -h/2f, 0);

        RenderSystem.setShaderTexture(0, icon);
        if (cd > 0) RenderSystem.setShaderColor(0.2f, 0.4f, 0.6f, 0.8f);
        else RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int s = (int)(w * 0.65f);
        g.blit(icon, (w-s)/2, (h-s)/2, 0, 0, s, s, s, s);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        pose.popPose();

        if (cd > 0) {
            int ch = (int)(h * cd);
            g.fill(0, h-ch, w, h, 0xA0000000);
            String t = "" + ((int)(max * cd / 20) + 1);
            pose.pushPose();
            pose.translate(w/2f, h/2f, 0);
            if (rot != 0) pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(-rot)));
            g.drawCenteredString(mc.font, t, 0, -4, 0xAAFFFFFF);
            pose.popPose();
        }

        if (!key.isEmpty()) {
            pose.pushPose(); pose.scale(0.45f, 0.45f, 0.45f);
            g.drawString(mc.font, "["+key+"]", 4, 4, 0xBBBBBB);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void drawRotatedLine(GuiGraphics g, int cx, int cy, int l, int t, float deg, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(deg)));
        pose.translate(-l/2f, -t/2f, 0);
        g.fill(0, 0, l, t, color);
        pose.popPose();
    }

    private static void renderLeftBuffs(GuiGraphics g, Minecraft mc, Player player, int sw, int sh) {
        CompoundTag nbt = player.getPersistentData();
        List<String> list = new ArrayList<>();
        if (nbt.getBoolean("OverloadActive")) {
            list.add(String.format("§d§l超载中 §f%.1fs", nbt.getInt("OverloadBuffTimer") / 20.0f));
        } else if (nbt.getInt("OverloadKillWindow") > 0) {
            int count = nbt.getInt("OverloadKillCount");
            list.add(String.format("§e§l准备超载 %d/5 §7(%ds)", count, nbt.getInt("OverloadKillWindow") / 20));
        }

        int x = 20, y = sh / 2 - 40;
        for (String s : list) {
            int tw = mc.font.width(s);
            g.fill(x - 4, y - 2, x + tw + 6, y + 10, 0x80000000);
            g.renderOutline(x - 4, y - 2, tw + 10, 12, 0xAAFFFFFF);
            g.drawString(mc.font, s, x, y, 0xFFFFFF, true);
            y += 16;
        }
    }
}