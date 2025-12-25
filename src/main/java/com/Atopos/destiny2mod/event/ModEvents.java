package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.item.custom.DestinyWeaponItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 击杀逻辑监听
        if (event.getSource().getEntity() instanceof Player player) {
            CompoundTag nbt = player.getPersistentData();

            // 只有在窗口期内且没有激活Buff时才计数
            if (nbt.getInt("OverloadKillWindow") > 0 && !nbt.getBoolean("OverloadActive")) {
                int kills = nbt.getInt("OverloadKillCount") + 1;
                nbt.putInt("OverloadKillCount", kills);

                if (kills >= 5) {
                    nbt.putBoolean("OverloadActive", true);
                    nbt.putInt("OverloadBuffTimer", 200); // Buff持续10秒
                    nbt.putInt("OverloadUsage", 0);       // 重置连续使用计数
                    nbt.putInt("OverloadKillWindow", 0);  // 关闭窗口
                    player.displayClientMessage(Component.literal("§d§l[超载] §f技能已进入无冷却状态！"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            CompoundTag nbt = player.getPersistentData();

            // 1. 杀敌窗口计时
            int window = nbt.getInt("OverloadKillWindow");
            if (window > 0) nbt.putInt("OverloadKillWindow", window - 1);

            // 2. 超载Buff持续计时
            int buffTimer = nbt.getInt("OverloadBuffTimer");
            if (buffTimer > 0) {
                nbt.putInt("OverloadBuffTimer", buffTimer - 1);
                if (buffTimer == 1) {
                    nbt.putBoolean("OverloadActive", false);
                    nbt.putInt("OverloadKillCount", 0); // 彻底清除计数，准备下一次循环
                    player.displayClientMessage(Component.literal("§c§l[超载] §f状态已结束"), true);
                }
            }
        }
    }

    // 原有的词条随机化逻辑保持不变
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof DestinyWeaponItem) {
                DestinyWeaponItem.randomizePerks(stack);
            }
        }
    }
}