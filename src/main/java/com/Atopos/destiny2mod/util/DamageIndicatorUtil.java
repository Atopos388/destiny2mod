package com.Atopos.destiny2mod.util;

import com.Atopos.destiny2mod.Destiny2Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 伤害数值指示器工具类
 * 现在已升级为自动监听器：只要世界上任何生物受到伤害，都会自动在头顶显示数字
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class DamageIndicatorUtil {

    /**
     * 核心监听器：监听全服所有生物受到的伤害
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只在服务端处理逻辑
        if (event.getEntity().level().isClientSide) return;

        // 获取伤害数值和目标实体
        float amount = event.getAmount();
        Entity target = event.getEntity();

        // 调用显示方法
        if (amount > 0.1f) {
            spawnIndicator(target, amount);
        }
    }

    /**
     * 在目标实体头顶生成伤害数字的具体逻辑
     * @param target 受到伤害的目标
     * @param damage 伤害数值
     */
    public static void spawnIndicator(Entity target, float damage) {
        if (target == null || target.level().isClientSide) return;

        Level level = target.level();

        // 计算生成位置：向上偏移实体高度，并加入随机抖动防止多个数字重叠
        double x = target.getX() + (level.random.nextDouble() - 0.5) * 0.7;
        double y = target.getY() + target.getBbHeight() + 0.1 + (level.random.nextDouble() * 0.2);
        double z = target.getZ() + (level.random.nextDouble() - 0.5) * 0.7;

        // 使用 AreaEffectCloud (区域效果云) 作为文字载体
        AreaEffectCloud indicator = new AreaEffectCloud(level, x, y, z);

        // 设置显示文本内容：红色(§c)，保留一位小数
        indicator.setCustomName(Component.literal(String.format("§c-%.1f", damage)));
        indicator.setCustomNameVisible(true);

        // 设置实体属性：无半径，持续 1 秒 (20 ticks)，立即消失
        indicator.setRadius(0f);
        indicator.setDuration(20);
        indicator.setWaitTime(0);

        // 将指示器加入世界
        level.addFreshEntity(indicator);
    }
}