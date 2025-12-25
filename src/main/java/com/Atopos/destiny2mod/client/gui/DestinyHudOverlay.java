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

        // 1. 渲染左侧 Buff 列表
        renderBuffs(guiGraphics, mc, player, 10, screenHeight / 2 - 40);

        // 2. 渲染底部技能
        int skillX = 20;
        int skillY = screenHeight - 40;
        renderAbility(guiGraphics, mc, player, skillX, skillY, GRENADE_ICON, ItemInit.SOLAR_GRENADE.get(), 200, partialTick);
        renderAbility(guiGraphics, mc, player, skillX + 30, skillY, MELEE_ICON, ItemInit.GHOST_GENERAL.get(), 100, partialTick);

        RenderSystem.disableBlend();
    };

    private static void renderBuffs(GuiGraphics guiGraphics, Minecraft mc, Player player, int x, int y) {
        CompoundTag nbt = player.getPersistentData();
        List<String> buffs = new ArrayList<>();

        if (nbt.getBoolean("OverloadActive")) {
            buffs.add(String.format("§d§l超载 §f%.1fs", nbt.getInt("OverloadBuffTimer") / 20.0f));
        } else if (nbt.getInt("OverloadKillWindow") > 0) {
            buffs.add(String.format("§e§l准备超载 §f%d/5 §7(%ds)", nbt.getInt("OverloadKillCount"), nbt.getInt("OverloadKillWindow") / 20));
        }

        int spacing = 0;
        for (String text : buffs) {
            guiGraphics.fill(x - 2, y + spacing - 1, x + mc.font.width(text) + 2, y + spacing + 9, 0x70000000);
            guiGraphics.drawString(mc.font, text, x, y + spacing, 0xFFFFFF, true);
            spacing += 12;
        }
    }

    private static void renderAbility(GuiGraphics guiGraphics, Minecraft mc, Player player, int x, int y, ResourceLocation icon, net.minecraft.world.item.Item item, int maxCooldown, float partialTick) {
        float cooldown = player.getCooldowns().getCooldownPercent(item, partialTick);
        int size = 22;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);
        if (cooldown > 0) RenderSystem.setShaderColor(0.4f, 0.4f, 0.4f, 1.0f);
        else RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(icon, x, y, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (cooldown > 0) {
            int h = (int) (size * cooldown);
            guiGraphics.fill(x, y + (size - h), x + size, y + size, 0x90000000);
            String cd = String.valueOf(((int)(maxCooldown * cooldown) / 20) + 1);
            guiGraphics.drawCenteredString(mc.font, cd, x + size / 2, y + (size - 8) / 2, 0xFFFFFF);
        } else guiGraphics.renderOutline(x - 1, y - 1, size + 2, size + 2, 0xAAFFFFFF);
    }
}