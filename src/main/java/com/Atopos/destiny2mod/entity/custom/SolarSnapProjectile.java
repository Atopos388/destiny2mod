package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.util.DamageIndicatorUtil;
import com.Atopos.destiny2mod.util.IgniteHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * 烈日响指火球实体
 * 修复版：支持多重命中结算
 */
public class SolarSnapProjectile extends ThrowableItemProjectile {
    private boolean isEnhanced = false;
    private long batchId = 0;

    public SolarSnapProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public SolarSnapProjectile(Level level, LivingEntity shooter) {
        super(EntityInit.SOLAR_SNAP_PROJECTILE.get(), shooter, level);
    }

    public void setEnhanced(boolean enhanced) {
        this.isEnhanced = enhanced;
    }

    public void setBatchId(long id) {
        this.batchId = id;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            int particleCount = isEnhanced ? 2 : 1;
            for(int i=0; i<particleCount; i++) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();

            // [核心修复] 重置目标的受伤无敌时间
            // 默认情况下实体受击后有 20 ticks 的无敌，这会导致 10 颗火球只有第 1 颗生效
            target.invulnerableTime = 0;

            float damage = isEnhanced ? 4.0F : 6.0F;

            // 执行伤害判定
            boolean hurtSuccess = target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

            if (hurtSuccess && this.getOwner() instanceof LivingEntity owner) {
                // 应用点燃层数
                IgniteHandler.apply(target, 10, owner);

                // 处理“爆燃护腿”刷新逻辑
                if (isEnhanced && owner instanceof Player player) {
                    CompoundTag data = player.getPersistentData();
                    String hitCounterKey = "destiny2mod.snap_hits_" + this.batchId;
                    String isRefreshedKey = "destiny2mod.snap_done_" + this.batchId;

                    int currentHits = data.getInt(hitCounterKey) + 1;
                    data.putInt(hitCounterKey, currentHits);

                    // 只有命中数达到 4 且该批次尚未刷新过时才执行
                    if (currentHits >= 4 && !data.getBoolean(isRefreshedKey)) {
                        if (player.getCooldowns().isOnCooldown(ItemInit.SOLAR_GRENADE.get())) {
                            player.getCooldowns().removeCooldown(ItemInit.SOLAR_GRENADE.get());
                            data.putBoolean(isRefreshedKey, true);

                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 2.0F);
                        }
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsEnhanced", isEnhanced);
        pCompound.putLong("BatchId", batchId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.isEnhanced = pCompound.getBoolean("IsEnhanced");
        this.batchId = pCompound.getLong("BatchId");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}