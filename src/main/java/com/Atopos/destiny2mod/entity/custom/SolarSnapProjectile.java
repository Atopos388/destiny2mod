package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.util.IgniteHandler;
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
import net.minecraftforge.network.NetworkHooks;

public class SolarSnapProjectile extends ThrowableItemProjectile {
    private boolean isEnhanced = false;
    private long batchId = 0;

    public SolarSnapProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) { super(type, level); }
    public SolarSnapProjectile(Level level, LivingEntity shooter) { super(EntityInit.SOLAR_SNAP_PROJECTILE.get(), shooter, level); }

    public void setEnhanced(boolean enhanced) { this.isEnhanced = enhanced; }
    public void setBatchId(long id) { this.batchId = id; }

    @Override protected Item getDefaultItem() { return Items.FIRE_CHARGE; }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity ownerEntity = this.getOwner();

            if (ownerEntity instanceof LivingEntity owner && target instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 0; // 核心：支持多重命中

                // 点燃规则：响指火球 3 层
                IgniteHandler.apply(livingTarget, 3, owner);

                float damage = isEnhanced ? 4.0F : 6.0F;
                boolean hurtSuccess = livingTarget.hurt(this.damageSources().thrown(this, owner), damage);

                // 爆燃护腿逻辑
                if (hurtSuccess && isEnhanced && owner instanceof Player player) {
                    CompoundTag data = player.getPersistentData();
                    String hitKey = "destiny2mod.snap_hits_" + this.batchId;
                    int hits = data.getInt(hitKey) + 1;
                    data.putInt(hitKey, hits);

                    if (hits >= 4 && !data.getBoolean("destiny2mod.snap_done_" + this.batchId)) {
                        if (player.getCooldowns().isOnCooldown(ItemInit.SOLAR_GRENADE.get())) {
                            player.getCooldowns().removeCooldown(ItemInit.SOLAR_GRENADE.get());
                            data.putBoolean("destiny2mod.snap_done_" + this.batchId, true);
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 2.0F);
                        }
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}