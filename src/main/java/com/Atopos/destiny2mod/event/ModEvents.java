package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.util.IgniteHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        IgniteHandler.spawnIgniteParticles(entity);

        CompoundTag data = entity.getPersistentData();
        // 1. 引信计时
        int fuse = data.getInt(IgniteHandler.FUSE_KEY);
        if (fuse > 0) {
            data.putInt(IgniteHandler.FUSE_KEY, fuse - 1);
            if (fuse == 1) IgniteHandler.triggerIgniteExplosion(entity, null);
        }
        // 2. 灼烧衰减
        int timer = data.getInt(IgniteHandler.TIMER_KEY);
        if (timer > 0) {
            data.putInt(IgniteHandler.TIMER_KEY, timer - 1);
            if (timer == 1) data.putInt(IgniteHandler.STACK_KEY, 0);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            CompoundTag nbt = player.getPersistentData();
            // 超载逻辑：戴头盔 + 窗口激活 + 非超载状态
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())
                    && nbt.getInt("OverloadKillWindow") > 0 && !nbt.getBoolean("OverloadActive")) {

                int kills = nbt.getInt("OverloadKillCount") + 1;
                nbt.putInt("OverloadKillCount", kills);

                if (kills >= 5) {
                    nbt.putBoolean("OverloadActive", true);
                    nbt.putInt("OverloadBuffTimer", 200);
                    nbt.putInt("OverloadKillWindow", 0);
                    nbt.putInt("OverloadUsage", 0);
                    player.displayClientMessage(Component.literal("§d§l[超载] §f光能已满盈！技能无冷却"), true);
                }
            }
        }
        // 死亡时如果处于引爆态，直接炸
        if (event.getEntity().getPersistentData().getInt(IgniteHandler.FUSE_KEY) > 0) {
            IgniteHandler.triggerIgniteExplosion(event.getEntity(), null);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            CompoundTag nbt = event.player.getPersistentData();
            // 倒计时逻辑
            int win = nbt.getInt("OverloadKillWindow");
            if (win > 0) nbt.putInt("OverloadKillWindow", win - 1);

            int buff = nbt.getInt("OverloadBuffTimer");
            if (buff > 0) {
                nbt.putInt("OverloadBuffTimer", buff - 1);
                if (buff == 1) {
                    nbt.putBoolean("OverloadActive", false);
                    nbt.putInt("OverloadKillCount", 0);
                }
            }
        }
    }
}