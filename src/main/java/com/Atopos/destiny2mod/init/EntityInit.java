package com.Atopos.destiny2mod.init;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.entity.custom.PerfectRetrogradeProjectile;
import com.Atopos.destiny2mod.entity.custom.SolarFlareEntity;
import com.Atopos.destiny2mod.entity.custom.SolarGrenadeProjectile;
import com.Atopos.destiny2mod.entity.custom.SolarSnapProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Destiny2Mod.MODID);

    // 1. 完美逆行子弹
    public static final RegistryObject<EntityType<PerfectRetrogradeProjectile>> PERFECT_RETROGRADE_PROJECTILE =
            ENTITIES.register("perfect_retrograde_projectile",
                    () -> EntityType.Builder.<PerfectRetrogradeProjectile>of(PerfectRetrogradeProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("perfect_retrograde_projectile"));

    // 2. 烈日手雷投掷物
    public static final RegistryObject<EntityType<SolarGrenadeProjectile>> SOLAR_GRENADE_PROJECTILE =
            ENTITIES.register("solar_grenade_projectile",
                    () -> EntityType.Builder.<SolarGrenadeProjectile>of(SolarGrenadeProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("solar_grenade_projectile"));

    // 3. 烈日耀斑区域
    public static final RegistryObject<EntityType<SolarFlareEntity>> SOLAR_FLARE_ENTITY =
            ENTITIES.register("solar_flare_entity",
                    () -> EntityType.Builder.<SolarFlareEntity>of(SolarFlareEntity::new, MobCategory.MISC)
                            .sized(3.0f, 2.0f)
                            .clientTrackingRange(10).updateInterval(20)
                            .fireImmune()
                            .build("solar_flare_entity"));

    // [新增] 4. 烈日响指火球实体
    public static final RegistryObject<EntityType<SolarSnapProjectile>> SOLAR_SNAP_PROJECTILE =
            ENTITIES.register("solar_snap_projectile",
                    () -> EntityType.Builder.<SolarSnapProjectile>of(SolarSnapProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10)
                            .build("solar_snap_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}