package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.util.IgniteHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;

/**
 * 烈日耀斑实体逻辑
 */
public class SolarFlareEntity extends Entity {
    private int lifeTime = 0;
    private final int MAX_LIFE_TIME = 100; // 5秒持续时间
    private LivingEntity owner;
    private UUID ownerUUID;

    public final AnimationState idleAnimationState = new AnimationState();

    public SolarFlareEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public SolarFlareEntity(Level level, Entity owner) {
        this(EntityInit.SOLAR_FLARE_ENTITY.get(), level);
        if (owner instanceof LivingEntity living) {
            this.setOwner(living);
        }
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.ownerUUID = owner != null ? owner.getUUID() : null;
    }

    @Override
    public void tick() {
        super.tick();

        // 客户端视觉效果
        if (this.level().isClientSide) {
            if (this.lifeTime == 0) {
                this.idleAnimationState.start(this.tickCount);
            }

            for (int i = 0; i < 2; i++) {
                double rx = (this.random.nextDouble() - 0.5) * 6.0;
                double rz = (this.random.nextDouble() - 0.5) * 6.0;
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + rx, this.getY() + 0.2, this.getZ() + rz, 0, 0.1, 0);
                this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
            }
        }

        lifeTime++;
        if (lifeTime >= MAX_LIFE_TIME) {
            this.discard();
            return;
        }

        // 服务端伤害逻辑
        if (!this.level().isClientSide) {
            if (lifeTime % 20 == 0) { // 每秒触发一次

                // [新增] 检查护甲增益
                boolean hasSunfireArmor = false;
                if (this.owner != null) {
                    // 检查胸甲槽位是否为炎阳护甲
                    hasSunfireArmor = this.owner.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.SUNFIRE_CHESTPLATE.get());
                }

                // 基础数值 vs 强化数值
                float damageAmount = hasSunfireArmor ? 4.0F : 2.0F; // 2颗心 vs 1颗心
                int scorchStacks = hasSunfireArmor ? 4 : 1;         // 4层 vs 1层

                AABB damageBox = this.getBoundingBox().inflate(4.0, 1.0, 4.0);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, damageBox);

                for (LivingEntity target : targets) {
                    if (target == this.owner) continue;

                    target.hurt(this.level().damageSources().inFire(), damageAmount);

                    // 应用点燃层数
                    IgniteHandler.apply(target, scorchStacks, this.owner);
                }
            }
        }
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.lifeTime = pCompound.getInt("LifeTime");
        if (pCompound.hasUUID("Owner")) this.ownerUUID = pCompound.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("LifeTime", this.lifeTime);
        if (this.ownerUUID != null) pCompound.putUUID("Owner", this.ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}