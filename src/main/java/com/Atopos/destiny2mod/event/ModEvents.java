package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.item.custom.DestinyWeaponItem;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.network.packet.S2CSyncOverloadPacket;
import com.Atopos.destiny2mod.util.IgniteHandler;
import com.Atopos.destiny2mod.util.ModDiagnostics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * 模组全局事件处理中心
 * 包含逻辑：点燃引擎、超载判定（含CD重置）、诊断心跳、数据同步、武器系统
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class ModEvents {

    /**
     * 生物 Tick 监听：处理点燃效果
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // 1. 生成点燃相关粒子
        IgniteHandler.spawnIgniteParticles(entity);

        CompoundTag data = entity.getPersistentData();

        // 2. 引信计时逻辑 (1秒/20ticks)
        int fuse = data.getInt(IgniteHandler.FUSE_KEY);
        if (fuse > 0) {
            data.putInt(IgniteHandler.FUSE_KEY, fuse - 1);
            if (fuse == 1) IgniteHandler.triggerIgniteExplosion(entity, null);
        }

        // 3. 灼烧层数衰减逻辑 (5秒不活动后清空)
        int timer = data.getInt(IgniteHandler.TIMER_KEY);
        if (timer > 0) {
            data.putInt(IgniteHandler.TIMER_KEY, timer - 1);
            if (timer == 1) data.putInt(IgniteHandler.STACK_KEY, 0);
        }
    }

    /**
     * 生物死亡监听：处理引爆与超载击杀计数
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        // 1. 死亡引爆：如果目标身上有正在燃烧的引信，死时立即爆炸
        if (victim.getPersistentData().getInt(IgniteHandler.FUSE_KEY) > 0) {
            IgniteHandler.triggerIgniteExplosion(victim, null);
        }

        // 2. [超载核心] 击杀计数逻辑
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            CompoundTag nbt = player.getPersistentData();

            // 判定：戴超载头盔 + 杀敌窗口活跃 + 未处于超载状态
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())
                    && nbt.getInt("OverloadKillWindow") > 0
                    && !nbt.getBoolean("OverloadActive")) {

                int kills = nbt.getInt("OverloadKillCount") + 1;
                nbt.putInt("OverloadKillCount", kills);

                // 立即同步击杀数，确保 UI 左侧数字跳动
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));

                if (kills >= 5) {
                    nbt.putBoolean("OverloadActive", true);
                    nbt.putInt("OverloadBuffTimer", 200); // 激活 10 秒超载
                    nbt.putInt("OverloadKillWindow", 0);
                    nbt.putInt("OverloadUsage", 0);

                    // === [新增核心逻辑] 进入超载瞬间重置技能冷却 ===
                    player.getCooldowns().removeCooldown(ItemInit.GHOST_GENERAL.get());
                    player.getCooldowns().removeCooldown(ItemInit.SOLAR_GRENADE.get());

                    // 激活瞬间全量同步
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                    player.displayClientMessage(Component.literal("§d§l[超载] §f光能已满盈！技能已重置。"), true);
                }
            }
        }
    }

    /**
     * 玩家 Tick 监听：处理 Buff 倒计时与 UI 强制刷除
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.player;
            CompoundTag nbt = player.getPersistentData();

            ModDiagnostics.LAST_SERVER_TICK = player.level().getGameTime();

            boolean needSync = false;

            // 1. 处理准备窗口 (黄色 Buff)
            int win = nbt.getInt("OverloadKillWindow");
            if (win > 0) {
                nbt.putInt("OverloadKillWindow", win - 1);
                if (win == 1) {
                    nbt.putInt("OverloadKillCount", 0);
                    needSync = true;
                } else if (win % 20 == 0) {
                    needSync = true;
                }
            }

            // 2. 处理超载激活状态 (紫色 Buff)
            int buf = nbt.getInt("OverloadBuffTimer");
            if (buf > 0) {
                nbt.putInt("OverloadBuffTimer", buf - 1);
                if (buf == 1) {
                    nbt.putBoolean("OverloadActive", false);
                    nbt.putInt("OverloadKillCount", 0);
                    nbt.putInt("OverloadUsage", 0);
                    needSync = true;
                    player.displayClientMessage(Component.literal("§c§l[超载] §f状态结束"), true);
                } else if (buf % 10 == 0) {
                    needSync = true;
                }
            }

            if (needSync) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
            }
        }
    }

    /**
     * 实体生成监听：处理武器词条随机化
     */
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