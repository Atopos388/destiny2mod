package com.Atopos.destiny2mod.util;

import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模组全功能深度诊断系统
 * 监测范围：注册表完整性、网络链路、玩家实时 Buff NBT、金装联动逻辑、事件总线心跳
 */
public class ModDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final List<DiagnosticEntry> REPORT = new ArrayList<>();
    public static boolean HAS_CRITICAL_ERROR = false;

    // 事件心跳监测位 (需在 ModEvents.onPlayerTick 中更新)
    public static long LAST_SERVER_TICK = 0;

    public static record DiagnosticEntry(String group, String name, boolean passed, String details) {}

    /**
     * 加载期的静态系统检查
     */
    public static void runSystemCheck() {
        REPORT.clear();
        HAS_CRITICAL_ERROR = false;
        scanRegistry();
        checkNetworkStatus();
        LOGGER.info("[DESTINY-MOD] 静态自检完成，严重错误: {}", HAS_CRITICAL_ERROR);
    }

    /**
     * 运行时的全系统深度扫描 (由诊断 UI 触发)
     */
    public static void runFullDiagnostic(Player player) {
        REPORT.clear();
        HAS_CRITICAL_ERROR = false;

        // 1. 扫描注册表 (物品与实体)
        scanRegistry();

        // 2. 监测网络链路
        checkNetworkStatus();

        // 3. 监测逻辑心跳 (检测 ModEvents 是否正常工作)
        checkEventHeartbeat(player);

        // 4. 监测玩家实时状态 (Buff 与 联动)
        if (player != null) {
            checkPlayerState(player);
        }
    }

    private static void scanRegistry() {
        check(ItemInit.OVERLOAD_HELMET, "注册", "超载头盔");
        check(ItemInit.IGNITION_LEGGINGS, "注册", "爆燃护腿");
        check(ItemInit.SUNFIRE_CHESTPLATE, "注册", "炎阳护甲");
        check(ItemInit.PERFECT_RETROGRADE, "注册", "完美逆行");
        check(EntityInit.SOLAR_SNAP_PROJECTILE, "实体", "烈日响指");
        check(EntityInit.SOLAR_FLARE_ENTITY, "实体", "烈日耀斑");
    }

    private static void checkNetworkStatus() {
        boolean packetOk = PacketHandler.INSTANCE != null;
        addEntry("网络", "数据包处理器", packetOk, packetOk ? "已就绪" : "未初始化!");
        if (!packetOk) HAS_CRITICAL_ERROR = true;
    }

    private static void checkEventHeartbeat(Player player) {
        long currentTime = player.level().getGameTime();
        // 如果服务器 Tick 超过 2 秒没更新心跳位，说明逻辑监听可能失效了
        boolean alive = (currentTime - LAST_SERVER_TICK) < 40;
        addEntry("核心", "事件总线心跳", alive, alive ? "逻辑响应中" : "检测到服务端逻辑卡死!");
    }

    private static void checkPlayerState(Player player) {
        CompoundTag nbt = player.getPersistentData();

        // --- 超载系统监测 ---
        boolean hasData = nbt.contains("OverloadKillWindow") || nbt.contains("OverloadActive");
        addEntry("Buff", "超载 NBT 链路", hasData, hasData ? "已连接" : "等待数据写入 (请释放技能)");

        if (nbt.getBoolean("OverloadActive")) {
            addEntry("Buff", "当前状态", true, "§d§l超载中§f - 剩余 " + (nbt.getInt("OverloadBuffTimer")/20) + "s | 惩罚计数: " + nbt.getInt("OverloadUsage") + "/5");
        } else if (nbt.getInt("OverloadKillWindow") > 0) {
            addEntry("Buff", "当前状态", true, "§e§l准备中§f - 击杀数: " + nbt.getInt("OverloadKillCount") + "/5 | 窗口: " + (nbt.getInt("OverloadKillWindow")/20) + "s");
        } else {
            addEntry("Buff", "当前状态", true, "空闲");
        }

        // --- 金装联动监测 ---
        boolean helmet = player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get());
        addEntry("联动", "超载头盔", helmet, helmet ? "已激活" : "未穿戴");

        boolean leggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());
        addEntry("联动", "爆燃护腿", leggings, leggings ? "已激活 (响指强化)" : "未穿戴");

        boolean chest = player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.SUNFIRE_CHESTPLATE.get());
        addEntry("联动", "炎阳护甲", chest, chest ? "已激活 (手雷强化)" : "未穿戴");
    }

    private static void check(RegistryObject<?> obj, String group, String name) {
        boolean ok = obj != null && obj.isPresent();
        addEntry(group, name, ok, ok ? "已载入" : "缺失!");
        if (!ok) HAS_CRITICAL_ERROR = true;
    }

    private static void addEntry(String group, String name, boolean passed, String details) {
        REPORT.add(new DiagnosticEntry(group, name, passed, details));
    }
}