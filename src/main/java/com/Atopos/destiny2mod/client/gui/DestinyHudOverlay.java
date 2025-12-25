package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Destiny HUD 覆盖层
 * 负责渲染手雷和近战技能的冷却图标
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DestinyHudOverlay {

    private static final ResourceLocation GRENADE_ICON = new ResourceLocation(Destiny2Mod.MODID, "textures/item/solar_grenade.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation("minecraft", "textures/item/fire_charge.png");

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "destiny_ability_hud", HUD_RENDERER);
    }

    public static final IGuiOverlay HUD_RENDERER = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isSpectator()) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        int startX = 20;
        int y = screenHeight - 40;
        int iconSize = 24;
        int spacing = 8;

        // 渲染手雷技能框
        renderSkillBox(guiGraphics, minecraft, player,
                startX, y, iconSize,
                GRENADE_ICON,
                ItemInit.SOLAR_GRENADE.get(),
                200,
                partialTick);

        // 渲染近战技能框
        int meleeX = startX + iconSize + spacing;
        renderSkillBox(guiGraphics, minecraft, player,
                meleeX, y, iconSize,
                MELEE_ICON,
                ItemInit.GHOST_GENERAL.get(),
                100,
                partialTick);

        RenderSystem.disableBlend();
    };

    private static void renderSkillBox(GuiGraphics guiGraphics, Minecraft mc, Player player,
                                       int x, int y, int size,
                                       ResourceLocation icon, net.minecraft.world.item.Item coolItem,
                                       int totalTicks, float partialTick) {

        float cooldown = player.getCooldowns().getCooldownPercent(coolItem, partialTick);

        RenderSystem.setShaderTexture(0, icon);
        if (cooldown > 0) {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        guiGraphics.blit(icon, x, y, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (cooldown > 0) {
            int coolHeight = (int) (size * cooldown);
            guiGraphics.fill(x, y + (size - coolHeight), x + size, y + size, 0x90000000);

            int remainingTicks = (int)(totalTicks * cooldown);
            if (remainingTicks > 0) {
                String timeStr = String.valueOf((remainingTicks / 20) + 1);
                int textWidth = mc.font.width(timeStr);
                guiGraphics.drawString(mc.font, timeStr, x + (size - textWidth) / 2, y + (size - 8) / 2, 0xFFFFFF);
            }
        } else {
            guiGraphics.renderOutline(x - 1, y - 1, size + 2, size + 2, 0xAAFFFFFF);
        }
    }
}