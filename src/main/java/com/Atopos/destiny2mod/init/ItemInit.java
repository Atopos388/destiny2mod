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

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Destiny2Mod.MODID);

    // --- 武器与弹药 ---
    public static final RegistryObject<Item> PERFECT_RETROGRADE = ITEMS.register("perfect_retrograde",
            () -> new PerfectRetrogradeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PERFECT_RETROGRADE_AMMO = ITEMS.register("perfect_retrograde_ammo",
            () -> new Item(new Item.Properties()));

    // --- 技能与机灵 ---
    public static final RegistryObject<Item> SOLAR_GRENADE = ITEMS.register("solar_grenade",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> GHOST_GENERAL = ITEMS.register("ghost_general",
            () -> new GhostItem(new Item.Properties().stacksTo(1)));

    // --- 异域金装 (核心) ---
    public static final RegistryObject<Item> SUNFIRE_CHESTPLATE = ITEMS.register("sunfire_chestplate",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> IGNITION_LEGGINGS = ITEMS.register("ignition_leggings",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC)));

    // [新增] 超载头盔
    public static final RegistryObject<Item> OVERLOAD_HELMET = ITEMS.register("overload_helmet",
            () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC)));

    // --- 资源 ---
    public static final RegistryObject<Item> GLIMMER = ITEMS.register("glimmer", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EXOTIC_ENGRAM = ITEMS.register("exotic_engram", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> STRANGE_COIN = ITEMS.register("strange_coin", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}