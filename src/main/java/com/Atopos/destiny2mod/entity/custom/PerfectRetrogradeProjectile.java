package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.item.perk.Perk;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public class PerfectRetrogradeProjectile extends ThrowableItemProjectile {

    public PerfectRetrogradeProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public PerfectRetrogradeProjectile(Level level, LivingEntity shooter) {
        super(EntityInit.PERFECT_RETROGRADE_PROJECTILE.get(), shooter, level);
    }

    // 将枪械的词条数据保存到实体 NBT 中
    public void setWeaponPerks(ItemStack gunStack) {
        if (gunStack.hasTag() && gunStack.getTag().contains("Perks")) {
            this.getPersistentData().put("SourcePerks", gunStack.getTag().getList("Perks", 8));
        }
    }

    // 辅助方法：从实体数据中加载词条列表
    private List<Perk> loadPerks() {
        List<Perk> perks = new ArrayList<>();
        CompoundTag data = this.getPersistentData();
        if (data.contains("SourcePerks")) {
            ListTag list = data.getList("SourcePerks", 8);
            for (int i = 0; i < list.size(); i++) {
                try {
                    perks.add(Perk.valueOf(list.getString(i)));
                } catch (Exception ignored) {}
            }
        }
        return perks;
    }

    @Override
    protected Item getDefaultItem() {
        return ItemInit.PERFECT_RETROGRADE_AMMO.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F; // 保持直线飞行
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() > 0.01D) {
            double horizontalDist = velocity.horizontalDistance();
            this.setYRot((float)(Mth.atan2(velocity.x, velocity.z) * (180D / Math.PI)));
            this.setXRot((float)(Mth.atan2(velocity.y, horizontalDist) * (180D / Math.PI)));
        }

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    // === [核心逻辑] 处理实体直接命中 ===
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;

        Entity target = result.getEntity();
        
        // 防止命中发射者自己
        if (target == this.getOwner()) return;
        
        List<Perk> perks = loadPerks();

        // 1. 计算伤害倍率 (基于词条)
        float damageMult = 1.0f;
        for (Perk p : perks) {
            damageMult *= p.getDamageMultiplier();
        }

        // === [修复编译错误] ===
        // 在 1.20.1 官方映射中，统一使用 invulnerableTime 来处理受击冷却
        // 只要将此值设为 0，实体就能立即再次受到伤害
        target.invulnerableTime = 0;

        // 2. 造成直接命中伤害：6.0 (对应 3 颗心)
        float directDamage = 6.0F * damageMult;
        boolean hurtSuccess = target.hurt(this.damageSources().thrown(this, this.getOwner()), directDamage);

        // 3. 蜻蜓 (DRAGONFLY) 词条逻辑：击杀后爆炸
        if (hurtSuccess && perks.contains(Perk.DRAGONFLY) && target instanceof LivingEntity livingTarget) {
            if (livingTarget.isDeadOrDying() || livingTarget.getHealth() <= 0) {
                // 击杀爆炸范围增加到 6.0 格，伤害增加到 12.0 (6颗心)
                performNoKnockbackExplosion(target.getX(), target.getY() + 1.0, target.getZ(), 6.0F, 12.0F, true);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // 普通撞击爆炸：3.0 伤害 (1.5颗心), 1.0 格范围
            performNoKnockbackExplosion(this.getX(), this.getY(), this.getZ(), 1.0F, 3.0F, false);
            this.discard();
        }
    }

    /**
     * 执行无击飞爆炸
     * @param radius 爆炸半径
     * @param maxDamage 中心最大伤害
     * @param isDragonfly 是否为词条触发的特殊爆炸（决定粒子效果）
     */
    private void performNoKnockbackExplosion(double x, double y, double z, float radius, float maxDamage, boolean isDragonfly) {
        Level level = this.level();

        // 特效与音效
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.5F, 1.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(isDragonfly ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
            if (isDragonfly) {
                // 将蜻蜓词条的粒子改为绿色 (HAPPY_VILLAGER)
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 60, 1.5, 1.5, 1.5, 0.1);
            }
        }

        // 手动计算范围内伤害
        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity == this.getOwner()) continue;

            // 同样使用 invulnerableTime = 0 来确保爆炸伤害不会被无敌帧吞掉
            entity.invulnerableTime = 0;

            double distSq = entity.distanceToSqr(x, y, z);
            if (distSq <= radius * radius) {
                // 简单的线性伤害衰减
                float distance = (float) Math.sqrt(distSq);
                float damageRatio = 1.0F - (distance / radius);
                float finalDamage = maxDamage * damageRatio;

                if (finalDamage > 0.1F) {
                    entity.hurt(level.damageSources().generic(), finalDamage);
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}