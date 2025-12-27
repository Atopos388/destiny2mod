package com.Atopos.destiny2mod.client.gui;

import com.Atopos.destiny2mod.util.ModDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 根目录式诊断终端
 * 目录结构：异域金装 | 技能系统 | 武器系统 | 系统内核
 */
public class DiagnosticScreen extends Screen {
    private String currentView = "ROOT";

    public DiagnosticScreen() {
        super(Component.literal("DESTINY-OS 内核诊断终端"));
    }

    @Override
    protected void init() {
        super.init();
        refresh();
    }

    private void refresh() {
        this.clearWidgets();
        if (Minecraft.getInstance().player != null) {
            ModDiagnostics.runFullDiagnostic(Minecraft.getInstance().player);
        }

        if (currentView.equals("ROOT")) {
            List<String> keys = new ArrayList<>(ModDiagnostics.CATEGORIES.keySet());
            int btnWidth = 200;
            int startY = (this.height / 2) - ((keys.size() * 24) / 2);

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                ModDiagnostics.DiagnosticCategory cat = ModDiagnostics.CATEGORIES.get(key);
                String prefix = cat.hasError() ? "§c[!] " : "§a[√] ";

                this.addRenderableWidget(Button.builder(Component.literal(prefix + cat.title), btn -> {
                    this.currentView = key;
                    this.refresh();
                }).bounds(this.width / 2 - 100, startY + (i * 24), btnWidth, 20).build());
            }
        } else {
            // 返回按钮
            this.addRenderableWidget(Button.builder(Component.literal("<< 返回根目录"), btn -> {
                this.currentView = "ROOT";
                this.refresh();
            }).bounds(x() + 10, y() + h() - 30, 100, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 25, 100, 20).build());
    }

    private int x() { return 30; }
    private int y() { return 20; }
    private int w() { return this.width - 60; }
    private int h() { return this.height - 70; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // 渲染外框
        graphics.fill(x() - 5, y() - 5, x() + w() + 5, y() + h() + 5, 0xEE050505);
        graphics.renderOutline(x() - 5, y() - 5, w() + 10, h() + 10, 0xFF33FF33);

        if (currentView.equals("ROOT")) {
            graphics.drawString(this.font, "§2[ROOT@DESTINY-OS]:~# system-diagnostics", x() + 10, y() + 10, 0xFFFFFF);
            graphics.drawString(this.font, "§7请选择模块查看详细数据:", x() + 10, y() + 25, 0xAAAAAA);
        } else {
            ModDiagnostics.DiagnosticCategory cat = ModDiagnostics.CATEGORIES.get(currentView);
            graphics.drawString(this.font, "§2[ROOT@DESTINY-OS]:/" + currentView.toLowerCase() + "#", x() + 10, y() + 10, 0xFFFFFF);

            int rowY = y() + 40;
            for (ModDiagnostics.DiagnosticEntry entry : cat.entries) {
                String status = entry.passed() ? "§a[√]" : "§c[X]";
                graphics.drawString(this.font, status + " §f" + entry.name(), x() + 15, rowY, 0xFFFFFF);
                graphics.drawString(this.font, "§b> " + entry.details(), x() + 160, rowY, 0xFFFFFF);
                rowY += 15;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}