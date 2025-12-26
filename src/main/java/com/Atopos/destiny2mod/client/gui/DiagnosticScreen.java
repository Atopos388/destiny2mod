package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.util.ModDiagnostics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 模组内嵌诊断终端
 * 修复：更新了方法名和变量引用以匹配最新的 ModDiagnostics 类
 */
public class DiagnosticScreen extends Screen {

    public DiagnosticScreen() {
        super(Component.literal("模组内核诊断终端"));
    }

    @Override
    protected void init() {
        super.init();
        // [修复] 匹配 ModDiagnostics 中的最新方法名 runSystemCheck
        ModDiagnostics.runSystemCheck();

        // 关闭按钮
        this.addRenderableWidget(Button.builder(Component.literal("关闭终端"), btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 40, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int x = 40;
        int y = 40;
        int terminalWidth = this.width - 80;
        int terminalHeight = this.height - 100;

        // 1. 绘制终端大背景 (深色透明)
        graphics.fill(x - 5, y - 5, x + terminalWidth + 5, y + terminalHeight + 5, 0xEE101010);
        graphics.renderOutline(x - 5, y - 5, terminalWidth + 10, terminalHeight + 10, 0xFF33FF33); // 绿边

        // 2. 绘制标题
        graphics.drawString(this.font, "§2[DESTINY-OS] §a核心注册表完整性扫描报告...", x + 10, y + 10, 0xFFFFFF);
        graphics.fill(x + 10, y + 22, x + terminalWidth - 10, y + 23, 0xFF33FF33);

        // 3. 渲染检测条目
        int rowY = y + 35;
        // [修复] 匹配 ModDiagnostics 中的 DiagnosticEntry 类型
        for (ModDiagnostics.DiagnosticEntry entry : ModDiagnostics.REPORT) {
            String status = entry.passed() ? "§2[  正常  ]" : "§c[  缺失!! ]";
            String color = entry.passed() ? "§7" : "§f";

            graphics.drawString(this.font, status, x + 10, rowY, 0xFFFFFF);
            graphics.drawString(this.font, color + entry.name(), x + 90, rowY, 0xFFFFFF);

            if (!entry.passed()) {
                graphics.drawString(this.font, "§4原因: " + entry.errorMsg(), x + 220, rowY, 0xFFFFFF);
            }

            rowY += 15;
        }

        // 4. 底部总结
        // [修复] 匹配 ModDiagnostics 中的 HAS_CRITICAL_ERROR 变量名
        if (ModDiagnostics.HAS_CRITICAL_ERROR) {
            graphics.drawString(this.font, "§c> 警报: 发现严重逻辑断层，请检查 ItemInit/EntityInit 注册表！", x + 10, y + terminalHeight - 20, 0xFFFFFF);
        } else {
            graphics.drawString(this.font, "§a> 诊断完毕: 所有光能组件运行良好。守望者，去战斗吧。", x + 10, y + terminalHeight - 20, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}