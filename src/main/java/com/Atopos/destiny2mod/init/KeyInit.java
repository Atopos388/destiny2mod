package com.Atopos.destiny2mod.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * 按键初始化类
 * <p>
 * 负责定义和初始化所有模组相关的按键映射
 * 这些按键映射会在游戏中用于触发各种技能和功能
 * </p>
 */
public class KeyInit {
    /**
     * 按键分类名称
     * 用于在游戏按键设置中分组显示模组按键
     */
    public static final String CATEGORY = "key.destiny2mod.category";

    /**
     * 手雷技能按键映射
     * <p>
     * 按键：G键
     * 功能：触发太阳能手雷技能
     * 冲突上下文：游戏中
     * </p>
     */
    public static final KeyMapping GRENADE_KEY = new KeyMapping(
            "key.destiny2mod.grenade_ability",      // 按键名称
            KeyConflictContext.IN_GAME,           // 冲突上下文
            InputConstants.Type.KEYSYM,           // 输入类型
            GLFW.GLFW_KEY_G,                      // 按键代码
            CATEGORY                              // 分类
    );

    /**
     * 近战技能按键映射
     * <p>
     * 按键：C键
     * 功能：触发太阳能近战技能（响指）
     * 冲突上下文：游戏中
     * </p>
     */
    public static final KeyMapping MELEE_KEY = new KeyMapping(
            "key.destiny2mod.melee_ability",       // 按键名称
            KeyConflictContext.IN_GAME,           // 冲突上下文
            InputConstants.Type.KEYSYM,           // 输入类型
            GLFW.GLFW_KEY_C,                      // 按键代码
            CATEGORY                              // 分类
    );

    /**
     * 诊断工具按键映射
     * <p>
     * 按键：F10键
     * 功能：打开模组诊断界面，显示运行状态和性能数据
     * 冲突上下文：游戏中
     * </p>
     */
    public static final KeyMapping DIAG_KEY = new KeyMapping(
            "key.destiny2mod.diagnostic_tool",      // 按键名称
            KeyConflictContext.IN_GAME,           // 冲突上下文
            InputConstants.Type.KEYSYM,           // 输入类型
            GLFW.GLFW_KEY_F10,                    // 按键代码
            CATEGORY                              // 分类
    );

    /**
     * 强能裂隙技能按键映射
     * <p>
     * 按键：V键
     * 功能：触发术士职业技能 - 强能裂隙
     * 冲突上下文：游戏中
     * </p>
     */
    public static final KeyMapping WELL_OF_RADIANCE_KEY = new KeyMapping(
            "key.destiny2mod.well_of_radiance",     // 按键名称
            KeyConflictContext.IN_GAME,           // 冲突上下文
            InputConstants.Type.KEYSYM,           // 输入类型
            GLFW.GLFW_KEY_V,                      // 按键代码
            CATEGORY                              // 分类
    );
}