package com.Atopos.destiny2mod.init;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.item.custom.GhostItem;
import com.Atopos.destiny2mod.item.custom.PerfectRetrogradeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册中心
 * 整合了武器、技能物品、机灵以及异域防具
 */
public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Destiny2Mod.MODID);

    // ================= [ 核心武器 ] =================

    // 完美逆行 - 脉冲步枪 (支持 GeckoLib 3D 渲染与三连发逻辑)
    public static final RegistryObject<Item> PERFECT_RETROGRADE = ITEMS.register("perfect_retrograde",
            () -> new PerfectRetrogradeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    // 虚空弹丸 (内部物品，用于渲染 PerfectRetrogradeProjectile 实体)
    public static final RegistryObject<Item> PERFECT_RETROGRADE_AMMO = ITEMS.register("perfect_retrograde_ammo",
            () -> new Item(new Item.Properties()));

    // ================= [ 技能与机灵 ] =================

    // 烈日手雷 (被 HUD 和投掷逻辑引用)
    public static final RegistryObject<Item> SOLAR_GRENADE = ITEMS.register("solar_grenade",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    // 通用机灵外壳 (用于打开机灵菜单)
    public static final RegistryObject<Item> GHOST_GENERAL = ITEMS.register("ghost_general",
            () -> new GhostItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // ================= [ 异域防具 ] =================

    // 炎阳护甲 (提供烈日耀斑增益)
    public static final RegistryObject<Item> SUNFIRE_CHESTPLATE = ITEMS.register("sunfire_chestplate",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.EPIC)));

    // 爆燃护腿 (提供烈日响指增益)
    public static final RegistryObject<Item> IGNITION_LEGGINGS = ITEMS.register("ignition_leggings",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.EPIC)));

    // ================= [ 基础资源 ] =================

    public static final RegistryObject<Item> GLIMMER = ITEMS.register("glimmer",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EXOTIC_ENGRAM = ITEMS.register("exotic_engram",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    // 奇怪的硬币
    public static final RegistryObject<Item> STRANGE_COIN = ITEMS.register("strange_coin",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ===========================================

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}