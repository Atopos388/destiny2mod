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
 * 命中后在地面生成一个 SolarFlareEntity (持续伤害场)
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
            // 命中后生成耀斑区域实体
            SolarFlareEntity flare = new SolarFlareEntity(this.level(), this.getOwner());
            flare.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(flare);

            this.discard();
        }
    }
}