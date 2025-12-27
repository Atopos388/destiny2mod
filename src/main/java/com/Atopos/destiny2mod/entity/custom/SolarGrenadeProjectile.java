package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * 烈日手雷投掷实体
 * 修复：解决了调用 SolarFlareEntity 构造器时的类型不匹配错误
 */
public class SolarGrenadeProjectile extends ThrowableItemProjectile {

    public SolarGrenadeProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public SolarGrenadeProjectile(Level level, LivingEntity shooter) {
        super(EntityInit.SOLAR_GRENADE_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // [修复] 必须确保 owner 是 LivingEntity 类型以匹配构造器
            if (this.getOwner() instanceof LivingEntity livingOwner) {
                SolarFlareEntity flare = new SolarFlareEntity(this.level(), livingOwner);
                flare.setPos(this.getX(), this.getY(), this.getZ());
                this.level().addFreshEntity(flare);
            }

            this.discard();
        }
    }
}