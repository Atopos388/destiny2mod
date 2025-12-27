package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.util.ModDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 模组内核全功能诊断终端
 */
public class DiagnosticScreen extends Screen {

    public DiagnosticScreen() {
        super(Component.literal("模组内核全功能诊断终端"));
    }

    @Override
    protected void init() {
        super.init();
        // 执行诊断扫描
        if (Minecraft.getInstance().player != null) {
            ModDiagnostics.runFullDiagnostic(Minecraft.getInstance().player);
        }

        this.addRenderableWidget(Button.builder(Component.literal("重新扫描"), btn -> {
            ModDiagnostics.runFullDiagnostic(Minecraft.getInstance().player);
        }).bounds(this.width / 2 - 110, this.height - 40, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("关闭终端"), btn -> this.onClose())
                .bounds(this.width / 2 + 10, this.height - 40, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = 40, y = 20;
        int terminalWidth = this.width - 80, terminalHeight = this.height - 70;

        // 绘制终端底板
        graphics.fill(x - 5, y - 5, x + terminalWidth + 5, y + terminalHeight + 5, 0xEE101010);
        graphics.renderOutline(x - 5, y - 5, terminalWidth + 10, terminalHeight + 10, 0xFF33FF33);

        graphics.drawString(this.font, "§2[DESTINY-OS] §a正在读取实时逻辑与 Buff 状态报告...", x + 10, y + 10, 0xFFFFFF);
        graphics.fill(x + 10, y + 22, x + terminalWidth - 10, y + 23, 0xFF33FF33);

        // 分类显示诊断条目
        int rowY = y + 35;
        for (ModDiagnostics.DiagnosticEntry entry : ModDiagnostics.REPORT) {
            String status = entry.passed() ? "§2[  OK  ]" : "§c[ FAIL ]";
            // 绘制分类前缀
            graphics.drawString(this.font, "§7" + String.format("%-6s", entry.group()), x + 10, rowY, 0xFFFFFF);
            graphics.drawString(this.font, status, x + 55, rowY, 0xFFFFFF);
            graphics.drawString(this.font, "§f" + entry.name(), x + 115, rowY, 0xFFFFFF);
            graphics.drawString(this.font, "§b> " + entry.details(), x + 250, rowY, 0xFFFFFF);

            rowY += 14;
            if (rowY > terminalHeight + y - 20) break; // 防止溢出屏幕
        }

        String footer = ModDiagnostics.HAS_CRITICAL_ERROR ? "§c警报: 发现严重功能异常，请检查配置！" : "§a系统监测: 所有战斗模组运行正常。";
        graphics.drawString(this.font, footer, x + 10, y + terminalHeight - 15, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}