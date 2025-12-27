package com.Atopos.destiny2mod.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/**
 * 烈日战斗引擎
 * 修复：增加了点燃所有权溯源，确保爆炸击杀计入玩家统计（用于超载触发）
 */
public class IgniteHandler {
    public static final String STACK_KEY = "destiny2mod.ignite_stacks";
    public static final String TIMER_KEY = "destiny2mod.ignite_timer";
    public static final String FUSE_KEY = "destiny2mod.ignite_fuse";
    public static final String OWNER_KEY = "destiny2mod.ignite_owner"; // 新增：记录点燃发起者

    public static void apply(Entity target, int amount, LivingEntity source) {
        if (!(target instanceof LivingEntity living) || target instanceof Player) return;
        CompoundTag data = living.getPersistentData();
        if (data.getInt(FUSE_KEY) > 0) return;

        // [核心修复] 存储最后一次施加灼烧的玩家 UUID
        if (source instanceof Player) {
            data.putUUID(OWNER_KEY, source.getUUID());
        }

        int next = data.getInt(STACK_KEY) + amount;

        if (next >= 10) {
            data.putInt(FUSE_KEY, 20); // 1秒引信
            data.putInt(STACK_KEY, 0);
            living.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.2F, 0.6F);
        } else {
            data.putInt(STACK_KEY, next);
            data.putInt(TIMER_KEY, 100);
        }
    }

    /**
     * 触发点燃爆炸
     * @param target 爆炸中心（受害者）
     * @param manualSource 手动指定的来源（通常由 ModEvents 传入 null，需从 NBT 读取）
     */
    public static void triggerIgniteExplosion(LivingEntity target, LivingEntity manualSource) {
        Level level = target.level();
        if (level.isClientSide) return;

        CompoundTag data = target.getPersistentData();
        LivingEntity finalSource = manualSource;

        // [核心修复] 如果没有直接来源，尝试从 NBT 中恢复点燃发起者
        if (finalSource == null && data.hasUUID(OWNER_KEY) && level instanceof ServerLevel sl) {
            Entity found = sl.getEntity(data.getUUID(OWNER_KEY));
            if (found instanceof LivingEntity) {
                finalSource = (LivingEntity) found;
            }
        }

        double x = target.getX(), y = target.getY() + 1.0, z = target.getZ();
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 0.5F);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, x, y, z, 50, 0.8, 0.8, 0.8, 0.2);
        }

        float damage = 14.0F;
        AABB area = new AABB(x-5, y-3, z-5, x+5, y+3, z+5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity e : targets) {
            if (e == finalSource || e instanceof Player) continue;

            // [核心修复] 使用 indirectMagic 并关联玩家，这样 ModEvents 就能抓到击杀者
            e.hurt(level.damageSources().indirectMagic(target, finalSource), damage);
            DamageIndicatorUtil.spawnIndicator(e, damage);

            if (e != target) apply(e, 5, finalSource);
        }

        // 清理数据
        data.putInt(FUSE_KEY, 0);
        data.remove(OWNER_KEY);
    }

    public static void spawnIgniteParticles(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel sl) {
            CompoundTag data = entity.getPersistentData();
            if (data.getInt(FUSE_KEY) > 0) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY()+1.2, entity.getZ(), 2, 0.2, 0.2, 0.2, 0.05);
            } else if (data.getInt(STACK_KEY) > 0 && entity.tickCount % 10 == 0) {
                sl.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY()+0.5, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }
}