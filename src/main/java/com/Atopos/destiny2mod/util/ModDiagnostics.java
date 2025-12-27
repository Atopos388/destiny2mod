package com.Atopos.destiny2mod.util;

import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.item.custom.DestinyWeaponItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

/**
 * 模组全功能分级诊断系统 (整合版)
 * 根目录：异域金装 | 技能系统 | 武器系统 | 系统内核
 */
public class ModDiagnostics {
    public static final Map<String, DiagnosticCategory> CATEGORIES = new LinkedHashMap<>();
    public static long LAST_SERVER_TICK = 0;

    public static record DiagnosticEntry(String name, boolean passed, String details) {}

    public static class DiagnosticCategory {
        public final String title;
        public final List<DiagnosticEntry> entries = new ArrayList<>();
        public DiagnosticCategory(String title) { this.title = title; }

        public boolean hasError() {
            return entries.stream().anyMatch(e -> !e.passed);
        }
    }

    public static void runSystemCheck() {
        runFullDiagnostic(null);
    }

    public static void runFullDiagnostic(Player player) {
        CATEGORIES.clear();
        CompoundTag nbt = (player != null) ? player.getPersistentData() : new CompoundTag();

        // 1. 异域金装模块 (Exotics) - 整合所有金装逻辑
        DiagnosticCategory exotics = new DiagnosticCategory("异域金装 (Exotics)");

        // --- 超载头盔 (Overload) ---
        checkItem(exotics, ItemInit.OVERLOAD_HELMET, "超载头盔: 注册");
        if (player != null) {
            boolean h = player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get());
            exotics.entries.add(new DiagnosticEntry("超载头盔: 佩戴", h, h ? "已激活" : "未穿戴"));
            if (h) {
                exotics.entries.add(new DiagnosticEntry("超载: 击杀进度", true, nbt.getInt("OverloadKillCount") + "/5"));
                exotics.entries.add(new DiagnosticEntry("超载: 惩罚计数", nbt.getInt("OverloadUsage") < 5, "当前: " + nbt.getInt("OverloadUsage")));
            }
        }

        // --- 炎阳护甲 (Sunfire) ---
        checkItem(exotics, ItemInit.SUNFIRE_CHESTPLATE, "炎阳护甲: 注册");
        if (player != null) {
            boolean c = player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.SUNFIRE_CHESTPLATE.get());
            exotics.entries.add(new DiagnosticEntry("炎阳护甲: 佩戴", c, c ? "伤害强化就绪" : "未穿戴"));
        }

        // --- 爆燃护腿 (Ignition) ---
        checkItem(exotics, ItemInit.IGNITION_LEGGINGS, "爆燃护腿: 注册");
        if (player != null) {
            boolean l = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());
            exotics.entries.add(new DiagnosticEntry("爆燃护腿: 佩戴", l, l ? "响指强化就绪" : "未穿戴"));
        }
        CATEGORIES.put("EXOTICS", exotics);

        // 2. 技能系统模块 (Skills)
        DiagnosticCategory skills = new DiagnosticCategory("技能系统 (Skills)");
        checkItem(skills, ItemInit.GHOST_GENERAL, "近战(机灵)注册");
        checkItem(skills, ItemInit.SOLAR_GRENADE, "手雷项目注册");
        if (player != null) {
            float meleeCD = player.getCooldowns().getCooldownPercent(ItemInit.GHOST_GENERAL.get(), 0);
            float grenadeCD = player.getCooldowns().getCooldownPercent(ItemInit.SOLAR_GRENADE.get(), 0);
            skills.entries.add(new DiagnosticEntry("机灵状态", meleeCD == 0, meleeCD > 0 ? "冷却中" : "可释放"));
            skills.entries.add(new DiagnosticEntry("手雷状态", grenadeCD == 0, grenadeCD > 0 ? "冷却中" : "可释放"));
        }
        CATEGORIES.put("SKILLS", skills);

        // 3. 武器系统模块 (Weapons & Perks)
        DiagnosticCategory weapons = new DiagnosticCategory("武器系统 (Weapons)");
        checkItem(weapons, ItemInit.PERFECT_RETROGRADE, "完美逆行注册");
        if (player != null) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof DestinyWeaponItem) {
                boolean randomized = held.getOrCreateTag().getBoolean("destiny2mod.perks_randomized");
                weapons.entries.add(new DiagnosticEntry("手持武器词条", randomized, randomized ? "已初始化" : "未随机化"));
            } else {
                weapons.entries.add(new DiagnosticEntry("手持武器", false, "非命运系列武器"));
            }
        }
        CATEGORIES.put("WEAPONS", weapons);

        // 4. 系统内核模块 (Core)
        DiagnosticCategory core = new DiagnosticCategory("系统内核 (Core)");
        boolean netOk = PacketHandler.INSTANCE != null;
        core.entries.add(new DiagnosticEntry("网络通道", netOk, netOk ? "OK" : "DISCONNECTED"));
        long currentTime = (player != null) ? player.level().getGameTime() : 0;
        boolean heartBeat = (currentTime - LAST_SERVER_TICK) < 40;
        core.entries.add(new DiagnosticEntry("逻辑心跳", heartBeat, heartBeat ? "活跃" : "异常"));
        CATEGORIES.put("CORE", core);
    }

    private static void checkItem(DiagnosticCategory cat, RegistryObject<?> obj, String name) {
        boolean ok = obj != null && obj.isPresent();
        cat.entries.add(new DiagnosticEntry(name, ok, ok ? "已载入" : "缺失!"));
    }
}