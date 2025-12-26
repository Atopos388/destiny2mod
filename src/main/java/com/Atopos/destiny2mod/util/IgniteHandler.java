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

public class IgniteHandler {
    public static final String STACK_KEY = "destiny2mod.ignite_stacks";
    public static final String TIMER_KEY = "destiny2mod.ignite_timer";
    public static final String FUSE_KEY = "destiny2mod.ignite_fuse";

    /**
     * 应用点燃层数 (玩家免疫)
     */
    public static void apply(Entity target, int amount, LivingEntity source) {
        if (!(target instanceof LivingEntity living) || target instanceof Player) return;

        CompoundTag data = living.getPersistentData();
        if (data.getInt(FUSE_KEY) > 0) return; // 已在引爆中

        int current = data.getInt(STACK_KEY);
        int next = current + amount;

        if (next >= 10) { // 核心规则：10层产生爆炸
            data.putInt(FUSE_KEY, 20); // 1秒引信
            data.putInt(STACK_KEY, 0);
            living.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.2F, 0.6F);
        } else {
            data.putInt(STACK_KEY, next);
            data.putInt(TIMER_KEY, 100); // 5秒衰减
        }
    }

    /**
     * 触发爆炸：12点伤害 + 传递5层点燃
     */
    public static void triggerIgniteExplosion(LivingEntity target, LivingEntity source) {
        Level level = target.level();
        double x = target.getX(), y = target.getY() + 1.0, z = target.getZ();

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 0.5F);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.FLAME, x, y, z, 50, 0.8, 0.8, 0.8, 0.2);
        }

        AABB area = new AABB(x-5, y-3, z-5, x+5, y+3, z+5);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity e : targets) {
            if (e == source || e instanceof Player) continue; // 不伤玩家

            e.hurt(level.damageSources().explosion(null, source), 12.0F);
            DamageIndicatorUtil.spawnIndicator(e, 12.0F);

            // 核心规则：向周围实体传递 5 层点燃
            if (e != target) {
                apply(e, 5, source);
            }
        }
    }

    /**
     * 生成点燃粒子的 Tick 逻辑
     */
    public static void spawnIgniteParticles(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel sl) {
            CompoundTag data = entity.getPersistentData();
            if (data.getInt(FUSE_KEY) > 0) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY()+1, entity.getZ(), 2, 0.2, 0.2, 0.2, 0.05);
            } else if (data.getInt(STACK_KEY) > 0 && entity.tickCount % 10 == 0) {
                sl.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY()+0.5, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.01);
            }
        }
    }
}