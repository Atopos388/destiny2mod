package com.Atopos.destiny2mod.entity.custom;

import com.Atopos.destiny2mod.init.EntityInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
// import software.bernie.geckolib.animatable.GeoEntity;
// import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
// import software.bernie.geckolib.core.animation.AnimatableManager;
// import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

/**
 * 强能裂隙实体类
 * <p>
 * 实现了 Destiny 2 中的强能裂隙效果，在范围内为玩家提供伤害和攻击速度提升
 * 继承自 Entity 类并实现 GeoEntity 接口，支持 GeckoLib 动画
 * </p>
 */
public class WellOfRadianceEntity extends Entity {
    /**
     * 伤害提升效果的 UUID
     * 用于唯一标识伤害提升效果，便于管理和移除
     */
    private static final UUID DAMAGE_BOOST_ID = UUID.fromString("e7f1b0c0-1a2b-3c4d-5e6f-7a8b9c0d1e2f");
    
    /**
     * 攻击速度提升效果的 UUID
     * 用于唯一标识攻击速度提升效果，便于管理和移除
     */
    private static final UUID ATTACK_SPEED_BOOST_ID = UUID.fromString("f8a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6");
    
    /**
     * 强能裂隙的生命周期
     * 单位：游戏刻（20游戏刻 = 1秒）
     * 默认值：300游戏刻（15秒）- 参考 Destiny 2 职业技能持续时间
     */
    private int lifetime = 300; // 15秒（300游戏刻）
    
    /**
     * GeckoLib 动画实例缓存
     * 用于存储和管理实体的动画实例
     */
    // private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    /**
     * 实体构造函数
     * <p>
     * 用于从实体类型和世界创建实体实例
     * 设置实体为无敌状态，防止被破坏
     * </p>
     * 
     * @param pEntityType 实体类型
     * @param pLevel 世界实例
     */
    public WellOfRadianceEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setInvulnerable(true); // 设置实体为无敌状态
    }
    
    /**
     * 实体构造函数（带所有者）
     * <p>
     * 用于从世界和所有者创建实体实例
     * 将实体生成在所有者的位置
     * </p>
     * 
     * @param pLevel 世界实例
     * @param pOwner 实体所有者
     */
    public WellOfRadianceEntity(Level pLevel, LivingEntity pOwner) {
        this(EntityInit.WELL_OF_RADIANCE_ENTITY.get(), pLevel);
        this.setPos(pOwner.getX(), pOwner.getY(), pOwner.getZ()); // 设置实体位置为所有者位置
    }
    
    /**
     * 实体 tick 方法
     * <p>
     * 每游戏刻调用一次，处理实体的主要逻辑
     * 客户端：生成粒子效果
     * 服务器：检测范围内玩家并添加增益效果
     * </p>
     */
    @Override
    public void tick() {
        super.tick();
        
        if (this.level().isClientSide) {
            // 客户端：生成增强的粒子效果
            
            // 1. 外部火圈 (半径 4.0，密集)
            if (this.tickCount % 2 == 0) { // 每2tick生成一次，增加密度
                int points = 60; // 点数增加
                double radius = 4.0; 
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    // 使用火焰粒子，紧贴地面 (y+0.1)
                    this.level().addParticle(ParticleTypes.FLAME, 
                        this.getX() + x, this.getY() + 0.1, this.getZ() + z, 
                        0, 0.01, 0); // 极小的向上速度
                }
            }

            // 2. 中心向外喷涌的能量流 (平面样式)
            int particleCount = 20; 
            for (int i = 0; i < particleCount; i++) {
                // 在中心附近随机生成
                double startRadius = this.random.nextDouble() * 1.5; // 初始半径 0-1.5
                double angle = this.random.nextDouble() * 2 * Math.PI;
                
                double offsetX = Math.cos(angle) * startRadius;
                double offsetZ = Math.sin(angle) * startRadius;
                
                // 计算向外的速度向量
                double speed = 0.1 + this.random.nextDouble() * 0.1; // 速度 0.1-0.2
                double velX = Math.cos(angle) * speed;
                double velZ = Math.sin(angle) * speed;

                // 橙色/红色粒子向外扩散，紧贴地面
                this.level().addParticle(
                        ParticleTypes.FLAME, 
                        this.getX() + offsetX, 
                        this.getY() + 0.1, // 紧贴地面
                        this.getZ() + offsetZ, 
                        velX, 0.0, velZ // 水平向外速度，Y轴速度为0
                );
                
                // 偶尔添加一点 Soul Fire Flame 增加层次感
                if (this.random.nextFloat() < 0.2) {
                     this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME, 
                        this.getX() + offsetX, 
                        this.getY() + 0.1, 
                        this.getZ() + offsetZ, 
                        velX * 0.8, 0.0, velZ * 0.8
                    );
                }
            }
        } else {
            // 服务器：检测范围内的玩家并添加效果
            // 创建 AABB 边界框，检测半径为 5 格
            AABB aabb = new AABB(
                    this.getX() - 5.0, this.getY() - 2.0, this.getZ() - 5.0,
                    this.getX() + 5.0, this.getY() + 3.0, this.getZ() + 5.0
            );
            
            // 获取范围内的所有玩家
            List<Player> players = this.level().getEntitiesOfClass(Player.class, aabb);
            for (Player player : players) {
                // 为玩家添加伤害提升效果（+1颗心）- 平衡调整：力量 I 足够强了
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false));
                // 为玩家添加挖掘速度提升效果（急迫），这将作为武器开火速度提升的标记
                // 等级 2 (amplifier 1)
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 1, false, false));
            }
            
            // 减少生命周期
            this.lifetime--;
            // 生命周期结束，移除实体
            if (this.lifetime <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }
    
    /**
     * 定义同步数据
     * <p>
     * 用于定义需要在客户端和服务器之间同步的实体数据
     * 本实体没有需要同步的额外数据，因此方法体为空
     * </p>
     */
    @Override
    protected void defineSynchedData() {}
    
    /**
     * 读取附加保存数据
     * <p>
     * 从 NBT 数据中读取实体的生命周期
     * </p>
     * 
     * @param pCompound NBT 复合标签
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.lifetime = pCompound.getInt("Lifetime");
    }
    
    /**
     * 添加附加保存数据
     * <p>
     * 将实体的生命周期保存到 NBT 数据中
     * </p>
     * 
     * @param pCompound NBT 复合标签
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("Lifetime", this.lifetime);
    }
    
    /**
     * 获取实体生成数据包
     * <p>
     * 返回用于在客户端生成实体的数据包
     * 使用 NetworkHooks 生成数据包，确保实体正确同步
     * </p>
     * 
     * @return 实体生成数据包
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    /**
     * 检查实体是否可被玩家拾取
     * <p>
     * 返回 false，表示实体不可被玩家拾取
     * </p>
     * 
     * @return 是否可被拾取
     */
    @Override
    public boolean isPickable() {
        return false;
    }
    
    /**
     * 注册控制器
     * <p>
     * 用于注册 GeckoLib 动画控制器
     * 本实体暂时不需要动画，因此方法体为空
     * </p>
     * 
     * @param controllers 控制器注册器
     */
    // @Override
    // public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    
    /**
     * 获取动画实例缓存
     * <p>
     * 返回实体的 GeckoLib 动画实例缓存
     * </p>
     * 
     * @return 动画实例缓存
     */
    // @Override
    // public AnimatableInstanceCache getAnimatableInstanceCache() {
    //     return this.cache;
    // }
}