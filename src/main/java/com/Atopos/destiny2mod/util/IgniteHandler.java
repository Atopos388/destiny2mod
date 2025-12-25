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
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID; // [修复] 导入 UUID

/**
 * 烈日系统 - 点燃与引燃处理器
 */
public class IgniteHandler {
    public static final String STACK_KEY = "destiny2mod.ignite_stacks";
    public static final String TIMER_KEY = "destiny2mod.ignite_timer";
    public static final String FUSE_KEY = "destiny2mod.ignite_fuse";
    public static final String IGNITER_UUID_KEY = "destiny2mod.igniter_uuid"; // [新增] 记录攻击者 UUID
    public static final int MAX_TIMER = 200;

    public static void apply(Entity target, int amount, LivingEntity source) {
        if (!(target instanceof LivingEntity living)) return;

        // 记录攻击者，以便后续爆炸时判定阵营
        if (source != null) {
            living.getPersistentData().putUUID(IGNITER_UUID_KEY, source.getUUID());
        }

        // 1. 友军豁免检查
        if (source != null) {
            if (target == source) return; // 不点燃自己
            if (source instanceof Player && target instanceof Player) return; // 玩家间不点燃
            if (target.isAlliedTo(source)) return; // 同队生物不点燃
        } else {
            // 如果来源未知（比如是被环境点燃的），安全起见不传染给玩家
            if (target instanceof Player) return;
        }

        CompoundTag data = living.getPersistentData();
        if (data.getInt(FUSE_KEY) > 0) return;

        int currentStacks = data.getInt(STACK_KEY);
        int newStacks = currentStacks + amount;

        if (newStacks >= 10) {
            data.putInt(FUSE_KEY, 20);
            data.putInt(STACK_KEY, 0);
            data.putInt(TIMER_KEY, 0);

            living.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.2F, 0.6F);
        } else {
            data.putInt(STACK_KEY, newStacks);
            data.putInt(TIMER_KEY, MAX_TIMER);
        }
    }

    public static void triggerIgniteExplosion(LivingEntity target, LivingEntity source) {
        Level level = target.level();

        // [关键修复] 尝试恢复攻击者 (Source)
        // 因为延时爆炸是由 Tick 事件触发的，此时 source 传进来是 null
        // 我们需要从 NBT 中读取之前保存的 UUID 来找回攻击者
        UUID sourceUUID = null;
        if (source != null) {
            sourceUUID = source.getUUID();
        } else {
            CompoundTag data = target.getPersistentData();
            if (data.hasUUID(IGNITER_UUID_KEY)) {
                sourceUUID = data.getUUID(IGNITER_UUID_KEY);
                if (level instanceof ServerLevel sl) {
                    Entity entity = sl.getEntity(sourceUUID);
                    if (entity instanceof LivingEntity) {
                        source = (LivingEntity) entity;
                    }
                }
            }
        }

        double x = target.getX();
        double y = target.getY() + target.getBbHeight()/2;
        double z = target.getZ();

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.5F, 0.7F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 60, 0.7, 0.7, 0.7, 0.2);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 20, 0.3, 0.3, 0.3, 0.1);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
        }

        float radius = 5.0F;
        float damage = 14.0F;

        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity e : targets) {
            // 2. 爆炸豁免检查 (双重保险)

            // 检查 UUID (最准确)
            if (sourceUUID != null && e.getUUID().equals(sourceUUID)) continue;

            if (source != null) {
                if (e == source) continue;
                if (source instanceof Player && e instanceof Player) continue;
                if (e.isAlliedTo(source)) continue;
            } else {
                // 如果实在找不到攻击者，为了防止误伤，不伤害任何玩家
                if (e instanceof Player) continue;
            }

            e.invulnerableTime = 0;
            e.hurt(level.damageSources().explosion(null, source), damage);

            // 传染逻辑
            apply(e, 5, source);
        }
    }

    public static void spawnGatheringParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() / 2;
        double z = entity.getZ();

        for (int i = 0; i < 4; i++) {
            double angle = serverLevel.random.nextDouble() * Math.PI * 2;
            double r = 1.2;
            double px = x + Math.cos(angle) * r;
            double pz = z + Math.sin(angle) * r;
            double py = y + (serverLevel.random.nextDouble() - 0.5) * 1.2;

            Vec3 motion = new Vec3(x - px, y - py, z - pz).normalize().scale(0.25);
            serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 0, motion.x, motion.y, motion.z, 0.1);
        }
    }
}