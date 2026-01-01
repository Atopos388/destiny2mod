package com.Atopos.destiny2mod.init;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.entity.custom.PerfectRetrogradeProjectile;
import com.Atopos.destiny2mod.entity.custom.SolarFlareEntity;
import com.Atopos.destiny2mod.entity.custom.SolarGrenadeProjectile;
import com.Atopos.destiny2mod.entity.custom.SolarSnapProjectile;
import com.Atopos.destiny2mod.entity.custom.WellOfRadianceEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体初始化类
 * <p>
 * 负责注册模组中所有的实体，包括投射物、技能效果实体等
 * 使用 DeferredRegister 延迟注册机制，确保在正确的时机注册实体
 * </p>
 */
public class EntityInit {
    /**
     * 实体延迟注册器
     * <p>
     * 用于延迟注册所有模组实体，确保在 Minecraft 注册系统准备就绪后再注册
     * </p>
     */
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Destiny2Mod.MODID);

    // ==================== 投射物实体 ====================
    /**
     * 完美逆行步枪子弹实体
     * <p>
     * 类型：投射物
     * 大小：0.25f x 0.25f
     * 客户端跟踪范围：4
     * 更新间隔：10游戏刻
     * 分类：MISC（杂项）
     * </p>
     */
    public static final RegistryObject<EntityType<PerfectRetrogradeProjectile>> PERFECT_RETROGRADE_PROJECTILE = 
            ENTITIES.register("perfect_retrograde_projectile",
                    () -> EntityType.Builder.<PerfectRetrogradeProjectile>of(PerfectRetrogradeProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("perfect_retrograde_projectile"));

    /**
     * 太阳能手雷投射物实体
     * <p>
     * 类型：投射物
     * 大小：0.25f x 0.25f
     * 客户端跟踪范围：4
     * 更新间隔：10游戏刻
     * 分类：MISC（杂项）
     * </p>
     */
    public static final RegistryObject<EntityType<SolarGrenadeProjectile>> SOLAR_GRENADE_PROJECTILE = 
            ENTITIES.register("solar_grenade_projectile",
                    () -> EntityType.Builder.<SolarGrenadeProjectile>of(SolarGrenadeProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("solar_grenade_projectile"));

    /**
     * 太阳能响指火球实体
     * <p>
     * 类型：投射物
     * 大小：0.25f x 0.25f
     * 客户端跟踪范围：4
     * 更新间隔：10游戏刻
     * 分类：MISC（杂项）
     * </p>
     */
    public static final RegistryObject<EntityType<SolarSnapProjectile>> SOLAR_SNAP_PROJECTILE = 
            ENTITIES.register("solar_snap_projectile",
                    () -> EntityType.Builder.<SolarSnapProjectile>of(SolarSnapProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("solar_snap_projectile"));

    // ==================== 技能效果实体 ====================
    /**
     * 烈日耀斑区域实体
     * <p>
     * 类型：技能效果实体
     * 大小：3.0f x 2.0f
     * 客户端跟踪范围：10
     * 更新间隔：20游戏刻
     * 特性：免疫火焰伤害
     * 分类：MISC（杂项）
     * </p>
     */
    public static final RegistryObject<EntityType<SolarFlareEntity>> SOLAR_FLARE_ENTITY = 
            ENTITIES.register("solar_flare_entity",
                    () -> EntityType.Builder.<SolarFlareEntity>of(SolarFlareEntity::new, MobCategory.MISC)
                            .sized(3.0f, 2.0f)
                            .clientTrackingRange(10).updateInterval(20)
                            .fireImmune()
                            .build("solar_flare_entity"));
    
    /**
     * 强能裂隙实体
     * <p>
     * 类型：技能效果实体
     * 大小：2.0f x 2.0f
     * 客户端跟踪范围：15
     * 更新间隔：20游戏刻
     * 特性：免疫火焰伤害
     * 分类：MISC（杂项）
     * 功能：在范围内为玩家提供伤害和攻击速度提升
     * </p>
     */
    public static final RegistryObject<EntityType<WellOfRadianceEntity>> WELL_OF_RADIANCE_ENTITY = 
            ENTITIES.register("well_of_radiance_entity",
                    () -> EntityType.Builder.<WellOfRadianceEntity>of(WellOfRadianceEntity::new, MobCategory.MISC)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(15).updateInterval(20)
                            .fireImmune()
                            .build("well_of_radiance_entity"));

    /**
     * 注册实体
     * <p>
     * 将所有实体注册到事件总线上，确保在正确的时机进行注册
     * </p>
     * 
     * @param eventBus 事件总线实例
     */
    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}