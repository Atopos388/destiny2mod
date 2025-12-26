package com.Atopos.destiny2mod.util;

import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.EntityInit;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模组内核诊断系统
 * 修复：统一方法名为 runSystemCheck 以匹配主类调用
 */
public class ModDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 用于 UI 显示的报告列表
    public static final List<DiagnosticEntry> REPORT = new ArrayList<>();
    public static boolean HAS_CRITICAL_ERROR = false;

    public static record DiagnosticEntry(String name, boolean passed, String errorMsg) {}

    public static void runSystemCheck() {
        REPORT.clear();
        HAS_CRITICAL_ERROR = false;
        LOGGER.info("================ [Destiny 2 Mod: 执行系统自检] ================");

        // 1. 检查核心物品注册状态
        check(ItemInit.OVERLOAD_HELMET, "超载头盔 (Overload Helmet)");
        check(ItemInit.IGNITION_LEGGINGS, "爆燃护腿 (Ignition Leggings)");
        check(ItemInit.SUNFIRE_CHESTPLATE, "炎阳护甲 (Sunfire Chestplate)");
        check(ItemInit.SOLAR_GRENADE, "烈日手雷 (Solar Grenade)");
        check(ItemInit.GHOST_GENERAL, "通用机灵 (Ghost)");

        // 2. 检查核心实体注册状态
        check(EntityInit.SOLAR_SNAP_PROJECTILE, "烈日响指火球");
        check(EntityInit.SOLAR_FLARE_ENTITY, "烈日耀斑伤害场");

        if (HAS_CRITICAL_ERROR) {
            LOGGER.error("[诊断结果] 发现严重错误：部分核心注册项缺失。");
        } else {
            LOGGER.info("[诊断结果] 扫描完成：系统运行状况良好。");
        }
        LOGGER.info("============================================================");
    }

    private static void check(RegistryObject<?> obj, String name) {
        boolean passed = false;
        String msg = "";
        try {
            if (obj != null && obj.isPresent()) {
                passed = true;
            } else {
                msg = "RegistryObject 缺失 (可能未在 ItemInit/EntityInit 中注册)";
                HAS_CRITICAL_ERROR = true;
            }
        } catch (Exception e) {
            msg = "检测异常: " + e.getMessage();
            HAS_CRITICAL_ERROR = true;
        }

        REPORT.add(new DiagnosticEntry(name, passed, msg));
    }
}