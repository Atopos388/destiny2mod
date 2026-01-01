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
 * <p>
 * 负责处理模组的核心事件逻辑，包括：
 * 1. 点燃引擎系统（粒子生成、引信计时、灼烧衰减）
 * 2. 超载判定与CD重置
 * 3. 诊断心跳机制
 * 4. 玩家数据同步
 * 5. 武器系统（词条随机化）
 * </p>
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class ModEvents {

    /**
     * 生物 Tick 事件监听器
     * <p>
     * 处理点燃效果相关的逻辑：
     * 1. 生成点燃相关粒子效果
     * 2. 处理引信计时（1秒/20游戏刻）
     * 3. 处理灼烧层数衰减（5秒不活动后清空）
     * </p>
     * 
     * @param event 生物Tick事件，包含当前生物实体信息
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        // 获取当前生物实体
        LivingEntity entity = event.getEntity();
        // 仅在服务器端执行逻辑，避免客户端重复计算
        if (entity.level().isClientSide) return;

        // 1. 生成点燃相关粒子
        IgniteHandler.spawnIgniteParticles(entity);

        // 获取实体的持久化数据
        CompoundTag data = entity.getPersistentData();

        // 2. 引信计时逻辑 (1秒/20游戏刻)
        int fuse = data.getInt(IgniteHandler.FUSE_KEY);
        if (fuse > 0) {
            // 引信倒计时减1
            data.putInt(IgniteHandler.FUSE_KEY, fuse - 1);
            // 当引信归零时，触发点燃爆炸
            if (fuse == 1) IgniteHandler.triggerIgniteExplosion(entity, null);
        }

        // 3. 灼烧层数衰减逻辑 (5秒不活动后清空)
        int timer = data.getInt(IgniteHandler.TIMER_KEY);
        if (timer > 0) {
            // 计时器减1
            data.putInt(IgniteHandler.TIMER_KEY, timer - 1);
            // 当计时器归零时，清空灼烧层数
            if (timer == 1) data.putInt(IgniteHandler.STACK_KEY, 0);
        }
    }

    /**
     * 生物死亡事件监听器
     * <p>
     * 处理生物死亡时的逻辑：
     * 1. 死亡引爆：如果目标身上有正在燃烧的引信，死时立即爆炸
     * 2. 超载核心逻辑：处理击杀计数和超载状态激活
     * </p>
     * 
     * @param event 生物死亡事件，包含受害者和伤害源信息
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 获取受害者实体
        LivingEntity victim = event.getEntity();
        // 仅在服务器端执行逻辑
        if (victim.level().isClientSide) return;

        // 1. 死亡引爆：如果目标身上有正在燃烧的引信，死时立即爆炸
        if (victim.getPersistentData().getInt(IgniteHandler.FUSE_KEY) > 0) {
            IgniteHandler.triggerIgniteExplosion(victim, null);
        }

        // 2. [超载核心] 击杀计数逻辑
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            // 获取玩家的持久化数据
            CompoundTag nbt = player.getPersistentData();

            // 判定：戴超载头盔 + 杀敌窗口活跃 + 未处于超载状态
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())
                    && nbt.getInt("OverloadKillWindow") > 0
                    && !nbt.getBoolean("OverloadActive")) {

                // 击杀计数+1
                int kills = nbt.getInt("OverloadKillCount") + 1;
                nbt.putInt("OverloadKillCount", kills);

                // 立即同步击杀数，确保 UI 左侧数字跳动
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));

                // 如果击杀数达到5个，激活超载状态
                if (kills >= 5) {
                    // 激活超载状态
                    nbt.putBoolean("OverloadActive", true);
                    // 设置超载持续时间为300游戏刻（15秒）
                    nbt.putInt("OverloadBuffTimer", 300);
                    // 关闭杀敌窗口
                    nbt.putInt("OverloadKillWindow", 0);
                    // 重置超载使用次数
                    nbt.putInt("OverloadUsage", 0);

                    // === [新增核心逻辑] 进入超载瞬间重置技能冷却 ===
                    player.getCooldowns().removeCooldown(ItemInit.GHOST_GENERAL.get());
                    player.getCooldowns().removeCooldown(ItemInit.SOLAR_GRENADE.get());
                    player.getCooldowns().removeCooldown(ItemInit.WELL_OF_RADIANCE.get());

                    // 激活瞬间全量同步数据到客户端
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                    // 向玩家发送激活超载的提示信息
                    player.displayClientMessage(Component.literal("§d§l[超载] §f光能已满盈！技能已重置。"), true);
                }
            }
        }
    }

    /**
     * 玩家 Tick 事件监听器
     * <p>
     * 处理玩家的状态逻辑：
     * 1. 更新诊断心跳时间
     * 2. 处理超载准备窗口倒计时
     * 3. 处理超载激活状态倒计时
     * 4. 管理数据同步逻辑
     * </p>
     * 
     * @param event 玩家Tick事件，包含玩家和当前游戏刻信息
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 仅在服务器端和Tick结束阶段执行逻辑
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.player;
            // 获取玩家的持久化数据
            CompoundTag nbt = player.getPersistentData();

            // 更新诊断心跳时间
            ModDiagnostics.LAST_SERVER_TICK = player.level().getGameTime();

            // 是否需要同步数据到客户端的标志
            boolean needSync = false;

            // 1. 处理准备窗口 (黄色 Buff)
            int win = nbt.getInt("OverloadKillWindow");
            if (win > 0) {
                // 准备窗口倒计时减1
                nbt.putInt("OverloadKillWindow", win - 1);
                // 当准备窗口结束时，重置击杀计数
                if (win == 1) {
                    nbt.putInt("OverloadKillCount", 0);
                    needSync = true;
                } else if (win % 20 == 0) {
                    // 每20游戏刻同步一次数据
                    needSync = true;
                }
            }

            // 2. 处理超载激活状态 (紫色 Buff)
            int buf = nbt.getInt("OverloadBuffTimer");
            if (buf > 0) {
                // 超载持续时间倒计时减1
                nbt.putInt("OverloadBuffTimer", buf - 1);
                // 当超载结束时，重置相关状态
                if (buf == 1) {
                    nbt.putBoolean("OverloadActive", false);
                    nbt.putInt("OverloadKillCount", 0);
                    nbt.putInt("OverloadUsage", 0);
                    needSync = true;
                    // 向玩家发送超载结束的提示信息
                    player.displayClientMessage(Component.literal("§c§l[超载] §f状态结束"), true);
                } else if (buf % 10 == 0) {
                    // 每10游戏刻同步一次数据，确保 UI 平滑
                    needSync = true;
                }
            }

            // 如果数据发生变化，同步到客户端
            if (needSync) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
            }
        }
    }

    /**
     * 实体加入世界事件监听器
     * <p>
     * 处理玩家加入服务器时的逻辑：
     * 1. 武器词条初始化：如果玩家手中的武器没有词条，随机生成一个
     * </p>
     * 
     * @param event 实体加入世界事件
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 仅在服务器端处理玩家实体
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            // 检查主手武器
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof DestinyWeaponItem) {
                // 如果武器没有词条NBT，进行初始化
                // randomizePerks 内部会检查是否存在 Perks 标签，所以这里可以直接调用
                DestinyWeaponItem.randomizePerks(mainHand);
            }
        }
    }
    
    // 移除 onEquipmentChange 方法
}
