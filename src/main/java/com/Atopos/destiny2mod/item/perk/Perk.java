package com.Atopos.destiny2mod.item.perk;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum Perk {
    // ================= 1号位：枪管 (影响伤害) =================
    // 参数：显示名, 分类, 颜色, 伤害倍率, 速度倍率, 射击间隔倍率
    FULL_BORE("全口径", Category.BARREL, ChatFormatting.RED, 1.15f, 1.0f, 1.0f),       // +15% 伤害
    CHAMBERED_COMPENSATOR("补偿器", Category.BARREL, ChatFormatting.RED, 0.95f, 1.0f, 1.0f), // -5% 伤害(模拟牺牲伤害换稳定性)
    HAMMER_FORGED("锻造膛线", Category.BARREL, ChatFormatting.RED, 1.05f, 1.0f, 1.0f), // +5% 伤害

    // ================= 2号位：弹夹 (影响速度/射速) =================
    HIGH_VELOCITY_ROUNDS("高速弹药", Category.MAGAZINE, ChatFormatting.GOLD, 1.0f, 1.3f, 1.0f), // +30% 飞行速度
    ASSAULT_MAG("突击弹匣", Category.MAGAZINE, ChatFormatting.GOLD, 1.0f, 1.0f, 0.8f),      // -20% 连发间隔 (射得更快)
    EXTENDED_MAG("延长弹匣", Category.MAGAZINE, ChatFormatting.GOLD, 1.0f, 1.0f, 1.2f),     // +20% 连发间隔 (射得更慢)

    // ================= 3号位：随机Buff A (独立池) =================
    OUTLAW("不法之徒", Category.TRAIT_1, ChatFormatting.YELLOW, 1.0f, 1.0f, 1.0f), // 击杀后换弹变快(暂未实现换弹)
    MOVING_TARGET("移动目标", Category.TRAIT_1, ChatFormatting.GREEN, 1.0f, 1.0f, 1.0f), // 移动时精度提高

    // ================= 4号位：随机Buff B (独立池) =================
    RAMPAGE("狂暴", Category.TRAIT_2, ChatFormatting.DARK_RED, 1.1f, 1.0f, 1.0f), // 简单起见，这里直接给10%常驻增伤
    DRAGONFLY("高爆载荷", Category.TRAIT_2, ChatFormatting.AQUA, 1.0f, 1.0f, 1.0f);   // 击杀产生爆炸

    private final String displayName;
    private final Category category;
    private final ChatFormatting color;
    private final float damageMultiplier; // 伤害倍率
    private final float speedMultiplier;  // 弹丸速度倍率
    private final float fireDelayMultiplier;// 射击间隔倍率

    Perk(String displayName, Category category, ChatFormatting color, float damageMult, float speedMult, float delayMult) {
        this.displayName = displayName;
        this.category = category;
        this.color = color;
        this.damageMultiplier = damageMult;
        this.speedMultiplier = speedMult;
        this.fireDelayMultiplier = delayMult;
    }

    public MutableComponent getColoredName() {
        return Component.literal(this.displayName).withStyle(this.color);
    }

    public Category getCategory() { return category; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getFireDelayMultiplier() { return fireDelayMultiplier; }

    // 定义分类枚举
    public enum Category {
        BARREL("枪管"),
        MAGAZINE("弹夹"),
        TRAIT_1("特性一"),
        TRAIT_2("特性二");

        private final String name;
        Category(String name) { this.name = name; }
        public String getName() { return name; }
    }
}