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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;

/**
 * 烈日耀斑区域实体
 * 补全逻辑：支持金装强化、动画播放、精准伤害溯源、UUID 持久化、网络包生成
 */
public class SolarFlareEntity extends Entity {
    private LivingEntity owner;
    private UUID ownerUUID;
    private int lifeTime = 0;
    private final int MAX_LIFE_TIME = 100; // 5秒
    public final AnimationState idleAnimationState = new AnimationState();

    public SolarFlareEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public SolarFlareEntity(Level level, LivingEntity owner) {
        this(EntityInit.SOLAR_FLARE_ENTITY.get(), level);
        this.setOwner(owner);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        if (owner != null) this.ownerUUID = owner.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        // 1. 处理动画与粒子 (仅客户端)
        if (this.level().isClientSide) {
            if (this.lifeTime == 0) {
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
            spawnClientParticles();
        }

        // 2. 处理生存周期
        this.lifeTime++;
        if (this.lifeTime > MAX_LIFE_TIME) {
            if (!this.level().isClientSide) this.discard();
            return;
        }

        // 3. 核心战斗逻辑 (每 20 tick 结算一次)
        if (!this.level().isClientSide && this.lifeTime % 20 == 0) {
            // [补全] 若 owner 丢失，尝试通过 UUID 重新寻址
            if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
                Entity found = serverLevel.getEntity(this.ownerUUID);
                if (found instanceof LivingEntity) this.owner = (LivingEntity) found;
            }

            // 检测炎阳护甲增强
            boolean isEnhanced = owner != null && owner.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.SUNFIRE_CHESTPLATE.get());

            float damageAmount = isEnhanced ? 4.0F : 2.0F; // 增强后翻倍至2心
            int scorchStacks = isEnhanced ? 4 : 2;        // 增强后每秒4层点燃

            AABB area = this.getBoundingBox().inflate(4.0, 1.5, 4.0);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity target : targets) {
                if (target != owner && !(target instanceof Player)) {
                    // 应用点燃层数
                    IgniteHandler.apply(target, scorchStacks, owner);

                    // [核心修复] 使用 indirectMagic 确保伤害源溯源到玩家，从而正确触发超载计数
                    target.hurt(this.level().damageSources().indirectMagic(this, owner), damageAmount);
                }
            }
        }
    }

    /**
     * 客户端粒子特效：渲染范围火圈与熔岩核心
     */
    private void spawnClientParticles() {
        if (this.tickCount % 2 == 0) {
            // 环形火焰
            for (int i = 0; i < 3; i++) {
                double angle = this.level().random.nextDouble() * Math.PI * 2;
                double r = 4.0;
                double px = this.getX() + Math.cos(angle) * r;
                double pz = this.getZ() + Math.sin(angle) * r;
                this.level().addParticle(ParticleTypes.FLAME, px, this.getY() + 0.1, pz, 0, 0.02, 0);
            }
            // 中心熔岩溅射
            this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {
        // 必要时在此添加同步数据
    }

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
        // [补全] 必须有这个，否则 Forge 无法正确同步实体生成
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}