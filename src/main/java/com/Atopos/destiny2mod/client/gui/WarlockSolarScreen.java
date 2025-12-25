package com.Atopos.destiny2mod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WarlockSolarScreen extends Screen {
    // === 调试模式开关 ===
    private final boolean debugMode = false;

    // === 资源路径定义 ===
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("destiny2mod", "textures/gui/subclass/solar_warlock_bg.png");
    private static final ResourceLocation SLOT_SUPER = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_super.png");
    private static final ResourceLocation SLOT_ABILITY = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_ability.png");
    private static final ResourceLocation SLOT_ASPECT = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_aspect.png");
    private static final ResourceLocation SLOT_FRAGMENT = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_fragment.png");

    private SubclassSlotButton superSlot;
    // 移动技能改为3个
    private SubclassSlotButton glideSlot1, glideSlot2, glideSlot3;
    private SubclassSlotButton classAbilitySlot;
    private SubclassSlotButton meleeSlot;
    private SubclassSlotButton grenadeSlot;
    // 被动改为2个大槽位
    private SubclassSlotButton aspectSlot1, aspectSlot2;

    public WarlockSolarScreen() {
        super(Component.translatable("gui.destiny2mod.subclass.warlock_solar"));
    }

    @Override
    protected void init() {
        super.init();

        // ================= 左侧区域 =================
        // 1. 大招 (Super) - 坐标: 178, 277, 大小: 200x200
        this.superSlot = this.addRenderableWidget(new SubclassSlotButton(
                sx(178), sy(277), s(200), s(200),
                SLOT_SUPER, Component.literal("黎明护盾 / 炎阳之井"),
                btn -> System.out.println("点击了大招")));
        this.superSlot.setTooltip(Tooltip.create(Component.literal("§6终极技能\n§7点击选择你的光能展现形式。")));

        // 2. 移动技能 (3个小槽位) - 位于大招下方
        // 坐标: 178, 515 (第一个), 间隔75 (253-178), 大小: 50x50
        int moveSize = s(50);
        int moveY = sy(515);

        this.glideSlot1 = this.addRenderableWidget(new SubclassSlotButton(sx(178), moveY, moveSize, moveSize,
                SLOT_ABILITY, Component.literal("爆发滑翔"), btn -> {}));
        this.glideSlot2 = this.addRenderableWidget(new SubclassSlotButton(sx(253), moveY, moveSize, moveSize,
                SLOT_ABILITY, Component.literal("平衡滑翔"), btn -> {}));
        this.glideSlot3 = this.addRenderableWidget(new SubclassSlotButton(sx(328), moveY, moveSize, moveSize,
                SLOT_ABILITY, Component.literal("控制滑翔"), btn -> {}));


        // ================= 中间区域 =================
        // 技能统一大小: 150x150, Y坐标: 302
        int midSize = s(150);
        int midY = sy(302);

        // 3. 职业技能 (Class Ability) - 坐标: 536
        this.classAbilitySlot = this.addRenderableWidget(new SubclassSlotButton(sx(536), midY, midSize, midSize,
                SLOT_ABILITY, Component.literal("职业技能"), btn -> {}));
        this.classAbilitySlot.setTooltip(Tooltip.create(Component.literal("§b职业技能\n§7按下[V]施放。")));

        // 4. 近战 (Melee) - 坐标: 760
        this.meleeSlot = this.addRenderableWidget(new SubclassSlotButton(sx(760), midY, midSize, midSize,
                SLOT_ABILITY, Component.literal("近战技能"), btn -> {}));

        // 5. 手雷 (Grenade) - 坐标: 980
        this.grenadeSlot = this.addRenderableWidget(new SubclassSlotButton(sx(980), midY, midSize, midSize,
                SLOT_ABILITY, Component.literal("手雷"), btn -> {}));


        // ================= 右侧区域 =================
        // 被动 (Aspects) - 坐标: 1308 和 1544, Y坐标: 365, 大小: 150x150
        int aspectSize = s(150);
        int aspectY = sy(365);

        this.aspectSlot1 = this.addRenderableWidget(new SubclassSlotButton(sx(1308), aspectY, aspectSize, aspectSize,
                SLOT_ASPECT, Component.literal("伊卡洛斯突进"), btn -> {}));

        this.aspectSlot2 = this.addRenderableWidget(new SubclassSlotButton(sx(1544), aspectY, aspectSize, aspectSize,
                SLOT_ASPECT, Component.literal("升温"), btn -> {}));


        // 返回按钮
        this.addRenderableWidget(Button.builder(Component.literal("返回"), btn -> this.onClose())
                .bounds(20, 20, 60, 20).build());
    }

    // === 坐标缩放工具方法 ===
    // 将 1920x1080 的坐标映射到当前屏幕尺寸
    private int sx(int x) { return (int)(x * (this.width / 1920.0f)); }
    private int sy(int y) { return (int)(y * (this.height / 1080.0f)); }
    // 大小缩放 (统一按宽度比例，保持正方形)
    private int s(int size) { return (int)(size * (this.width / 1920.0f)); }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        RenderSystem.enableBlend();
        // 铺满背景
        guiGraphics.blit(BG_TEXTURE, 0, 0, this.width, this.height, 0, 0, 1920, 1080, 1920, 1080);

        // 绘制连线 (大招 -> 职业技能)
        int lineColor = 0xAAFFDD88;
        drawLineToStandard(guiGraphics, superSlot, classAbilitySlot, lineColor);

        // 绘制标题
        // 左上角大标题 "术士"
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        guiGraphics.drawString(this.font, "§6术士", sx(238) / 2, sy(130) / 2, 0xFFFFD900);
        guiGraphics.pose().popPose();

        // 绘制中间技能的标签 (文字坐标 Y=257)
        drawCenteredLabel(guiGraphics, "职业技能", sx(536), sy(257), s(150));
        drawCenteredLabel(guiGraphics, "近战", sx(760), sy(257), s(150));
        drawCenteredLabel(guiGraphics, "手雷", sx(980), sy(257), s(150));

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (debugMode) {
            drawDebugOverlay(guiGraphics, mouseX, mouseY);
        }
    }

    private void drawCenteredLabel(GuiGraphics guiGraphics, String text, int x, int y, int width) {
        // 计算中心点 X
        int centerX = x + width / 2;
        guiGraphics.drawCenteredString(this.font, text, centerX, y, 0xFFFFFF);
    }

    // === 下面的辅助方法保持不变 ===
    private void drawLineToStandard(GuiGraphics guiGraphics, Button startBtn, Button endBtn, int color) {
        int x1 = startBtn.getX() + startBtn.getWidth() / 2;
        int y1 = startBtn.getY() + startBtn.getHeight() / 2;
        int x2 = endBtn.getX() + endBtn.getWidth() / 2;
        int y2 = endBtn.getY() + endBtn.getHeight() / 2;
        drawLine(guiGraphics, x1, y1, x2, y2, 2, color);
    }

    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int width, int color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x1, y1, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float)angle));
        guiGraphics.fill(0, -width/2, (int)length, width/2, color);
        guiGraphics.pose().popPose();
    }

    private void drawDebugOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String absCoords = "Screen: " + this.width + "x" + this.height + " | Mouse: " + mouseX + ", " + mouseY;
        guiGraphics.drawString(this.font, absCoords, 10, 10, 0x00FF00);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private class SubclassSlotButton extends Button {
        private final ResourceLocation iconTexture;

        public SubclassSlotButton(int x, int y, int width, int height, ResourceLocation icon, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.iconTexture = icon;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, this.iconTexture);
            guiGraphics.blit(this.iconTexture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

            if (debugMode) guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF00FF00);

            if (this.isHoveredOrFocused()) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
                guiGraphics.renderOutline(this.getX()-1, this.getY()-1, this.width+2, this.height+2, 0xFFFFD700);
            }
            RenderSystem.disableBlend();
        }
    }
}