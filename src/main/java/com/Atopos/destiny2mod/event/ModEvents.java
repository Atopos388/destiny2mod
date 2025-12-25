package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.item.custom.DestinyWeaponItem;
import com.Atopos.destiny2mod.util.IgniteHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof DestinyWeaponItem) {
                DestinyWeaponItem.randomizePerks(stack);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        CompoundTag data = entity.getPersistentData();

        // 1. 引燃维持与视觉特效
        if (entity.tickCount % 5 == 0) { // 每 0.25秒 检测一次视觉效果
            int stacks = data.getInt(IgniteHandler.STACK_KEY);

            // [新增] 纯视觉燃烧逻辑
            if (stacks > 0 && entity.level() instanceof ServerLevel serverLevel) {
                // 在实体身上生成火焰粒子，模拟"着火"的样子
                // 粒子数量随层数增加而增加
                int particleCount = Math.max(1, stacks / 3);

                for(int i = 0; i < particleCount; i++) {
                    double dx = (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
                    double dy = entity.getRandom().nextDouble() * entity.getBbHeight();
                    double dz = (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();

                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz,
                            1, 0, 0.05, 0, 0.0);
                }
            }
        }

        // 计时器逻辑 (每秒运行)
        if (entity.tickCount % 20 == 0) {
            int timer = data.getInt(IgniteHandler.TIMER_KEY);
            if (timer > 0) {
                int newTimer = timer - 20;
                data.putInt(IgniteHandler.TIMER_KEY, newTimer);
                if (newTimer <= 0) {
                    data.putInt(IgniteHandler.STACK_KEY, 0);
                    // 不需要 clearFire()，因为我们压根没 setSecondsOnFire
                }
            }
        }

        // 2. 引燃爆发延迟处理
        int fuse = data.getInt(IgniteHandler.FUSE_KEY);
        if (fuse > 0) {
            IgniteHandler.spawnGatheringParticles(entity);
            int newFuse = fuse - 1;
            data.putInt(IgniteHandler.FUSE_KEY, newFuse);

            if (newFuse == 0) {
                IgniteHandler.triggerIgniteExplosion(entity, null);
            }
        }
    }
}