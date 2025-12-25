package com.Atopos.destiny2mod.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class IgniteHandler {
    public static final String STACK_KEY = "destiny2mod.ignite_stacks";
    public static final String TIMER_KEY = "destiny2mod.ignite_timer";
    public static final String FUSE_KEY = "destiny2mod.ignite_fuse";
    public static final int MAX_TIMER = 200;

    public static void apply(Entity target, int amount, LivingEntity source) {
        if (!(target instanceof LivingEntity living)) return;
        CompoundTag data = living.getPersistentData();
        if (data.getInt(FUSE_KEY) > 0) return;

        int current = data.getInt(STACK_KEY);
        int next = current + amount;

        if (next >= 10) {
            data.putInt(FUSE_KEY, 20);
            data.putInt(STACK_KEY, 0);
            data.putInt(TIMER_KEY, 0);
            living.level().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.2F, 0.6F);
        } else {
            data.putInt(STACK_KEY, next);
            data.putInt(TIMER_KEY, MAX_TIMER);
        }
    }

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
            if (e == source) continue;
            e.hurt(level.damageSources().explosion(null, source), 12.0F);
            // 爆炸向周围传递 3 层灼烧
            if (e != target) apply(e, 3, source);
        }
    }

    public static void spreadScorchOnDeath(LivingEntity victim) {
        Level level = victim.level();
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LAVA, victim.getX(), victim.getY() + 1, victim.getZ(), 8, 0.4, 0.4, 0.4, 0.1);
        }
        AABB spreadArea = victim.getBoundingBox().inflate(3.5);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, spreadArea);
        for (LivingEntity e : nearby) {
            if (e != victim) apply(e, 2, null);
        }
    }

    public static void spawnGatheringParticles(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 1, entity.getZ(), 5, 0.3, 0.5, 0.3, 0.05);
        }
    }
}