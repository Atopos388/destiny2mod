package com.Atopos.destiny2mod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 术士太阳能子类界面类
 * <p>
 * 负责渲染术士太阳能子类的技能选择界面，包括：
 * 1. 大招（Super）选择
 * 2. 移动技能选择（3个滑翔选项）
 * 3. 职业技能选择（强能裂隙）
 * 4. 近战技能选择
 * 5. 手雷选择
 * 6. 被动技能（Aspects）选择
 * </p>
 */
public class WarlockSolarScreen extends Screen {
    // === 调试模式开关 ===
    private final boolean debugMode = false;

    // === 资源路径定义 ===
    /** 背景纹理资源定位器 */
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation("destiny2mod", "textures/gui/subclass/solar_warlock_bg.png");
    /** 大招槽位纹理资源定位器 */
    private static final ResourceLocation SLOT_SUPER = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_super.png");
    /** 技能槽位纹理资源定位器 */
    private static final ResourceLocation SLOT_ABILITY = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_ability.png");
    /** 被动技能槽位纹理资源定位器 */
    private static final ResourceLocation SLOT_ASPECT = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_aspect.png");
    /** 碎片槽位纹理资源定位器 */
    private static final ResourceLocation SLOT_FRAGMENT = new ResourceLocation("destiny2mod", "textures/gui/subclass/slot_fragment.png");

    /** 大招槽位按钮 */
    private SubclassSlotButton superSlot;
    /** 移动技能槽位按钮（3个） */
    private SubclassSlotButton glideSlot1, glideSlot2, glideSlot3;
    /** 职业技能槽位按钮（强能裂隙） */
    private SubclassSlotButton classAbilitySlot;
    /** 近战技能槽位按钮 */
    private SubclassSlotButton meleeSlot;
    /** 手雷槽位按钮 */
    private SubclassSlotButton grenadeSlot;
    /** 被动技能槽位按钮（2个） */
    private SubclassSlotButton aspectSlot1, aspectSlot2;

    /**
     * 构造函数
     * <p>
     * 初始化术士太阳能子类界面，设置界面标题
     * </p>
     */
    public WarlockSolarScreen() {
        super(Component.translatable("gui.destiny2mod.subclass.warlock_solar"));
    }

    /**
     * 初始化界面元素
     * <p>
     * 在这个方法中，所有的技能槽位按钮都被创建和添加到界面上
     * 包括左侧区域的大招和移动技能，中间区域的职业技能、近战和手雷，右侧区域的被动技能
     * </p>
     */
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

        // 3. 职业技能 (Class Ability) - 坐标: 536 - 强能裂隙
        this.classAbilitySlot = this.addRenderableWidget(new SubclassSlotButton(sx(536), midY, midSize, midSize,
                SLOT_ABILITY, Component.literal("强能裂隙"), btn -> {}));
        this.classAbilitySlot.setTooltip(Tooltip.create(Component.literal("§b强能裂隙\n§7按下[V]施放。\n§a+2颗心伤害提升\n§a+1攻击速度提升")));

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
    /**
     * 将X坐标从1920x1080基准分辨率缩放到当前屏幕分辨率
     * 
     * @param x 1920x1080基准分辨率下的X坐标
     * @return 当前屏幕分辨率下的X坐标
     */
    private int sx(int x) { return (int)(x * (this.width / 1920.0f)); }
    
    /**
     * 将Y坐标从1920x1080基准分辨率缩放到当前屏幕分辨率
     * 
     * @param y 1920x1080基准分辨率下的Y坐标
     * @return 当前屏幕分辨率下的Y坐标
     */
    private int sy(int y) { return (int)(y * (this.height / 1080.0f)); }
    
    /**
     * 将大小从1920x1080基准分辨率缩放到当前屏幕分辨率
     * 统一按宽度比例缩放，保持正方形
     * 
     * @param size 1920x1080基准分辨率下的大小
     * @return 当前屏幕分辨率下的大小
     */
    private int s(int size) { return (int)(size * (this.width / 1920.0f)); }

    /**
     * 渲染界面
     * <p>
     * 在这个方法中，渲染界面的各个元素：
     * 1. 背景
     * 2. 技能连线
     * 3. 标题
     * 4. 技能标签
     * 5. 调试覆盖层（如果启用了调试模式）
     * </p>
     * 
     * @param guiGraphics GuiGraphics对象，用于绘制界面元素
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTick 部分刻进度（0.0-1.0）
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        this.renderBackground(guiGraphics);

        // 设置背景纹理并启用混合模式
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        RenderSystem.enableBlend();
        // 铺满背景，将1920x1080的纹理拉伸到当前屏幕尺寸
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

        // 渲染所有界面元素（按钮等）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 如果启用了调试模式，绘制调试覆盖层
        if (debugMode) {
            drawDebugOverlay(guiGraphics, mouseX, mouseY);
        }
    }

    /**
     * 绘制居中标签
     * <p>
     * 在指定位置绘制居中的标签文本
     * </p>
     * 
     * @param guiGraphics GuiGraphics对象，用于绘制文本
     * @param text 要绘制的文本
     * @param x 标签区域的X坐标
     * @param y 标签区域的Y坐标
     * @param width 标签区域的宽度
     */
    private void drawCenteredLabel(GuiGraphics guiGraphics, String text, int x, int y, int width) {
        // 计算中心点 X
        int centerX = x + width / 2;
        // 在中心点绘制文本
        guiGraphics.drawCenteredString(this.font, text, centerX, y, 0xFFFFFF);
    }

    /**
     * 绘制连线（从起始按钮到目标按钮）
     * <p>
     * 绘制一条从起始按钮中心到目标按钮中心的连线
     * </p>
     * 
     * @param guiGraphics GuiGraphics对象，用于绘制连线
     * @param startBtn 起始按钮
     * @param endBtn 目标按钮
     * @param color 连线颜色
     */
    private void drawLineToStandard(GuiGraphics guiGraphics, Button startBtn, Button endBtn, int color) {
        // 计算起始点坐标（按钮中心）
        int x1 = startBtn.getX() + startBtn.getWidth() / 2;
        int y1 = startBtn.getY() + startBtn.getHeight() / 2;
        // 计算目标点坐标（按钮中心）
        int x2 = endBtn.getX() + endBtn.getWidth() / 2;
        int y2 = endBtn.getY() + endBtn.getHeight() / 2;
        // 绘制连线
        drawLine(guiGraphics, x1, y1, x2, y2, 2, color);
    }

    /**
     * 绘制直线
     * <p>
     * 绘制一条指定起点、终点、宽度和颜色的直线
     * </p>
     * 
     * @param guiGraphics GuiGraphics对象，用于绘制直线
     * @param x1 起始点X坐标
     * @param y1 起始点Y坐标
     * @param x2 终点X坐标
     * @param y2 终点Y坐标
     * @param width 直线宽度
     * @param color 直线颜色
     */
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int width, int color) {
        // 计算直线角度
        double angle = Math.atan2(y2 - y1, x2 - x1);
        // 计算直线长度
        double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        
        // 保存当前变换矩阵
        guiGraphics.pose().pushPose();
        // 平移到起始点
        guiGraphics.pose().translate(x1, y1, 0);
        // 旋转到直线角度
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float)angle));
        // 绘制直线（使用填充矩形）
        guiGraphics.fill(0, -width/2, (int)length, width/2, color);
        // 恢复变换矩阵
        guiGraphics.pose().popPose();
    }

    /**
     * 绘制调试覆盖层
     * <p>
     * 在屏幕左上角显示当前屏幕尺寸和鼠标坐标
     * </p>
     * 
     * @param guiGraphics GuiGraphics对象，用于绘制调试信息
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    private void drawDebugOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 构建调试信息字符串
        String absCoords = "Screen: " + this.width + "x" + this.height + " | Mouse: " + mouseX + ", " + mouseY;
        // 绘制调试信息
        guiGraphics.drawString(this.font, absCoords, 10, 10, 0x00FF00);
    }

    /**
     * 检查界面是否是暂停界面
     * <p>
     * 此界面不是暂停界面，因此返回false
     * </p>
     * 
     * @return 是否是暂停界面，总是返回false
     */
    @Override
    public boolean isPauseScreen() { return false; }

    /**
     * 自定义技能槽位按钮内部类
     * <p>
     * 用于渲染技能槽位按钮，包括：
     * 1. 技能图标
     * 2. 悬停效果
     * 3. 调试边框（如果启用了调试模式）
     * </p>
     */
    private class SubclassSlotButton extends Button {
        /** 按钮图标纹理资源定位器 */
        private final ResourceLocation iconTexture;

        /**
         * 构造函数
         * 
         * @param x 按钮X坐标
         * @param y 按钮Y坐标
         * @param width 按钮宽度
         * @param height 按钮高度
         * @param icon 按钮图标纹理资源定位器
         * @param message 按钮消息
         * @param onPress 按钮点击事件处理器
         */
        public SubclassSlotButton(int x, int y, int width, int height, ResourceLocation icon, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.iconTexture = icon;
        }

        /**
         * 渲染按钮
         * <p>
         * 在这个方法中，渲染按钮的各个元素：
         * 1. 按钮图标
         * 2. 调试边框（如果启用了调试模式）
         * 3. 悬停效果（半透明覆盖层和边框）
         * </p>
         * 
         * @param guiGraphics GuiGraphics对象，用于绘制按钮元素
         * @param mouseX 鼠标X坐标
         * @param mouseY 鼠标Y坐标
         * @param partialTick 部分刻进度（0.0-1.0）
         */
        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 启用混合模式
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // 设置按钮图标纹理
            RenderSystem.setShaderTexture(0, this.iconTexture);
            // 绘制按钮图标
            guiGraphics.blit(this.iconTexture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

            // 如果启用了调试模式，绘制调试边框
            if (debugMode) guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF00FF00);

            // 如果按钮被悬停或聚焦，绘制悬停效果
            if (this.isHoveredOrFocused()) {
                // 绘制半透明白色覆盖层
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
                // 绘制金色边框
                guiGraphics.renderOutline(this.getX()-1, this.getY()-1, this.width+2, this.height+2, 0xFFFFD700);
            }
            // 禁用混合模式
            RenderSystem.disableBlend();
        }
    }
}