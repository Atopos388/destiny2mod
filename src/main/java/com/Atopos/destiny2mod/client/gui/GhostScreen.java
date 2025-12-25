package com.Atopos.destiny2mod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

// 确保导入了新的界面类
import com.Atopos.destiny2mod.client.gui.WarlockSolarScreen;

public class GhostScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("destiny2mod", "textures/gui/ghost_ui.png");

    public GhostScreen() {
        super(Component.translatable("gui.destiny2mod.ghost_title"));
    }

    @Override
    protected void init() {
        super.init();

        int btnWidth = 120; // 稍微加宽一点
        int btnHeight = 24; // 稍微加高一点
        int spacing = 12;

        int xPos = (int) (this.width * 0.75) - (btnWidth / 2);
        int startY = (this.height - (btnHeight * 3 + spacing * 2)) / 2;

        // === 使用自定义的 GhostButton 选择职业 ===

        // 1. 术士 (Warlock) - 对应 lang 文件中的 button.nav
        this.addRenderableWidget(new GhostButton(xPos, startY, btnWidth, btnHeight,
                Component.translatable("gui.destiny2mod.button.nav"),
                (button) -> selectClass("术士")));

        // 2. 猎人 (Hunter) - 对应 lang 文件中的 button.quest
        this.addRenderableWidget(new GhostButton(xPos, startY + btnHeight + spacing, btnWidth, btnHeight,
                Component.translatable("gui.destiny2mod.button.quest"),
                (button) -> selectClass("猎人")));

        // 3. 泰坦 (Titan) - 对应 lang 文件中的 button.vehicle
        this.addRenderableWidget(new GhostButton(xPos, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight,
                Component.translatable("gui.destiny2mod.button.vehicle"),
                (button) -> selectClass("泰坦")));
    }

    // === 处理职业选择逻辑 ===
    private void selectClass(String className) {
        if (this.minecraft != null && this.minecraft.player != null) {
            Player player = this.minecraft.player;

            if (className.equals("术士")) {
                // 播放一个音效增加代入感 (可选)
                // player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                // 打开术士技能界面
                // setScreen 会自动关闭当前的 GhostScreen
                this.minecraft.setScreen(new WarlockSolarScreen());

            } else {
                // 对于还没做的猎人和泰坦，暂时发送提示
                player.sendSystemMessage(Component.literal("§b[机灵] §f数据传输中断..."));
                player.sendSystemMessage(Component.literal("§c✖ 无法加载职业模组: §7" + className + " (开发中)"));
                this.onClose();
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        RenderSystem.setShaderTexture(0, TEXTURE);
        // 渲染高清背景
        guiGraphics.blit(TEXTURE, 0, 0, this.width, this.height, 0, 0, 1920, 1080, 1920, 1080);

        renderPlayerModel(guiGraphics, mouseX, mouseY);
        renderInfoText(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    private void renderPlayerModel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        int playerX = (int) (this.width * 0.25);
        int playerY = (int) (this.height * 0.85);
        int scale = 80;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics, playerX, playerY, scale,
                (float)(playerX) - mouseX, (float)(playerY - 130) - mouseY,
                this.minecraft.player);
    }

    private void renderInfoText(GuiGraphics guiGraphics) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int textX = 20;
        int textY = this.height / 2 - 60;
        int lineHeight = 12;

        guiGraphics.drawString(this.font, "§7位置信息:", textX, textY, 0xFFFFFF);
        guiGraphics.drawString(this.font, "X: " + (int)player.getX(), textX, textY + lineHeight, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Y: " + (int)player.getY(), textX, textY + lineHeight * 2, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Z: " + (int)player.getZ(), textX, textY + lineHeight * 3, 0xFFFFFF);

        String biome = player.level().getBiome(player.blockPosition()).unwrapKey().get().location().getPath();
        guiGraphics.drawString(this.font, "§7环境: §e" + biome, textX, textY + lineHeight * 5, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // === 自定义按钮内部类 ===
    // 这种写法让我们不需要额外的贴图文件就能画出好看的按钮
    class GhostButton extends Button {
        public GhostButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 判断鼠标是否悬停在按钮上
            boolean isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                    mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            // 1. 绘制背景
            // 悬停时：半透明白色 (0x40FFFFFF)
            // 普通时：深黑色半透明 (0xAA000000)
            int bgColor = isHovered ? 0x40FFFFFF : 0xAA000000;
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

            // 2. 绘制边框 (命运2风格)
            // 悬停时：青色 (0xFF00FFFF)
            // 普通时：灰色 (0xFF555555)
            int borderColor = isHovered ? 0xFF00AAAA : 0xFF555555;
            guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

            // 3. 绘制文字
            // 居中显示
            int textColor = isHovered ? 0xFFFFFF55 : 0xFFFFFFFF; // 悬停变黄
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                    this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2,
                    textColor);
        }
    }
}