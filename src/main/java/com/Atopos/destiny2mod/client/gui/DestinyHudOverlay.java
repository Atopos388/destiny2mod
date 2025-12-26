package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 高级 Destiny 2 风格 HUD 渲染
 * 修复：删除了错误的 ClientModEvents 声明
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DestinyHudOverlay {

    private static final ResourceLocation GRENADE_ICON = new ResourceLocation(Destiny2Mod.MODID, "textures/item/solar_grenade.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation("minecraft", "textures/item/fire_charge.png");

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "destiny_hud_main", HUD_RENDERER);
    }

    public static final IGuiOverlay HUD_RENDERER = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.player.isSpectator()) return;

        Player player = mc.player;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 1. 渲染左侧动态 Buff 列表
        renderAdvancedBuffs(guiGraphics, mc, player, 20, screenHeight / 2 - 30);

        // 2. 渲染左下角技能组
        int baseY = screenHeight - 55;
        int baseX = 25;

        // 烈日职业主题装饰条
        guiGraphics.fill(baseX - 4, baseY - 6, baseX + 65, baseY - 4, 0xFFFF9800);
        guiGraphics.fill(baseX - 4, baseY - 4, baseX - 2, baseY + 30, 0xFFFF9800);

        // 手雷 [G]
        renderAbilitySlot(guiGraphics, mc, player, baseX, baseY, GRENADE_ICON, ItemInit.SOLAR_GRENADE.get(), 200, partialTick, "G");

        // 近战 [C]
        renderAbilitySlot(guiGraphics, mc, player, baseX + 34, baseY, MELEE_ICON, ItemInit.GHOST_GENERAL.get(), 100, partialTick, "C");

        RenderSystem.disableBlend();
    };

    private static void renderAdvancedBuffs(GuiGraphics graphics, Minecraft mc, Player player, int x, int y) {
        CompoundTag nbt = player.getPersistentData();
        int offset = 0;

        if (nbt.getBoolean("OverloadActive")) {
            int timer = nbt.getInt("OverloadBuffTimer");
            float pct = timer / 200.0f;
            drawD2Buff(graphics, mc, x, y + offset, "§d§l超载", String.format("%.1fs", timer / 20.0f), pct, 0xFFBB00FF);
            offset += 20;
        }

        if (nbt.getInt("OverloadKillWindow") > 0 && !nbt.getBoolean("OverloadActive")) {
            int count = nbt.getInt("OverloadKillCount");
            float pct = nbt.getInt("OverloadKillWindow") / 200.0f;
            drawD2Buff(graphics, mc, x, y + offset, "§e§l准备超载", count + " / 5", pct, 0xFFFFEB3B);
            offset += 20;
        }
    }

    private static void drawD2Buff(GuiGraphics g, Minecraft mc, int x, int y, String title, String info, float progress, int color) {
        int w = 90;
        g.fill(x, y, x + w, y + 16, 0x80000000);
        g.drawString(mc.font, title, x + 5, y + 4, 0xFFFFFF, true);
        g.drawString(mc.font, info, x + w - mc.font.width(info) - 5, y + 4, 0xFFFFFF, true);
        g.fill(x, y + 15, x + (int)(w * progress), y + 16, color);
    }

    private static void renderAbilitySlot(GuiGraphics g, Minecraft mc, Player player, int x, int y, ResourceLocation icon, net.minecraft.world.item.Item item, int maxCD, float pt, String key) {
        float cd = player.getCooldowns().getCooldownPercent(item, pt);
        int s = 28;

        g.fill(x, y, x + s, y + s, 0x90101010);
        RenderSystem.setShaderTexture(0, icon);
        if (cd > 0) {
            RenderSystem.setShaderColor(0.4f, 0.4f, 0.4f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int alpha = (int)(Math.abs(Math.sin(player.tickCount * 0.1)) * 30 + 30);
            g.fill(x, y, x + s, y + s, alpha << 24 | 0x00FF9800);
        }
        g.blit(icon, x + 4, y + 4, 0, 0, 20, 20, 20, 20);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (cd > 0) {
            int h = (int)(s * cd);
            g.fill(x, y + (s - h), x + s, y + s, 0xCC000000);
            String sec = String.valueOf((int)(maxCD * cd / 20) + 1);
            g.drawCenteredString(mc.font, sec, x + s/2, y + s/2 - 4, 0xFFFFFF);
        } else {
            g.renderOutline(x, y, s, s, 0xCCFFFFFF);
        }

        g.pose().pushPose();
        g.pose().scale(0.5f, 0.5f, 0.5f);
        g.drawString(mc.font, "[" + key + "]", (x + 2) * 2, (y + s - 7) * 2, 0xBBBBBB, false);
        g.pose().popPose();
    }
}