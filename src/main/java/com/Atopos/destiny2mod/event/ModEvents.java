package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.item.custom.DestinyWeaponItem;
import com.Atopos.destiny2mod.util.IgniteHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
        CompoundTag data = entity.getPersistentData();

        // 1. 点燃与灼烧计时
        int fuse = data.getInt(IgniteHandler.FUSE_KEY);
        if (fuse > 0) {
            data.putInt(IgniteHandler.FUSE_KEY, fuse - 1);
            IgniteHandler.spawnGatheringParticles(entity);
            if (fuse == 1) IgniteHandler.triggerIgniteExplosion(entity, null);
        }

        int scorchTimer = data.getInt(IgniteHandler.TIMER_KEY);
        if (scorchTimer > 0) {
            data.putInt(IgniteHandler.TIMER_KEY, scorchTimer - 1);
            if (scorchTimer == 1) data.putInt(IgniteHandler.STACK_KEY, 0);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        CompoundTag data = victim.getPersistentData();

        // 点燃连锁处理
        if (data.getInt(IgniteHandler.FUSE_KEY) > 0) {
            IgniteHandler.triggerIgniteExplosion(victim, null);
        } else if (data.getInt(IgniteHandler.STACK_KEY) > 0) {
            IgniteHandler.spreadScorchOnDeath(victim);
        }

        // 超载头盔击杀判定
        if (event.getSource().getEntity() instanceof Player player) {
            CompoundTag nbt = player.getPersistentData();
            if (nbt.getInt("OverloadKillWindow") > 0 && !nbt.getBoolean("OverloadActive")) {
                int kills = nbt.getInt("OverloadKillCount") + 1;
                nbt.putInt("OverloadKillCount", kills);
                if (kills >= 5) {
                    nbt.putBoolean("OverloadActive", true);
                    nbt.putInt("OverloadBuffTimer", 200);
                    nbt.putInt("OverloadUsage", 0);
                    nbt.putInt("OverloadKillWindow", 0);
                    player.displayClientMessage(Component.literal("§d§l[超载] §f技能已进入无冷却状态！"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            CompoundTag nbt = event.player.getPersistentData();
            int window = nbt.getInt("OverloadKillWindow");
            if (window > 0) nbt.putInt("OverloadKillWindow", window - 1);

            int buff = nbt.getInt("OverloadBuffTimer");
            if (buff > 0) {
                nbt.putInt("OverloadBuffTimer", buff - 1);
                if (buff == 1) {
                    nbt.putBoolean("OverloadActive", false);
                    nbt.putInt("OverloadKillCount", 0);
                    event.player.displayClientMessage(Component.literal("§c§l[超载] §f状态结束"), true);
                }
            }
        }
    }

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