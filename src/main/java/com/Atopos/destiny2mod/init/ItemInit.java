package com.Atopos.destiny2mod.init;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.item.custom.DestinyArmorItem;
import com.Atopos.destiny2mod.item.custom.GhostItem;
import com.Atopos.destiny2mod.item.custom.PerfectRetrogradeItem;
import com.Atopos.destiny2mod.item.custom.WellOfRadianceItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品初始化类
 * <p>
 * 负责注册模组中所有的物品，包括武器、技能物品、装备和资源
 * 使用 DeferredRegister 延迟注册机制，确保在正确的时机注册物品
 * </p>
 */
public class ItemInit {
    /**
     * 物品延迟注册器
     * <p>
     * 用于延迟注册所有模组物品，确保在 Minecraft 注册系统准备就绪后再注册
     * </p>
     */
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, Destiny2Mod.MODID);

    // ==================== 武器 ====================
    /**
     * 完美逆行步枪
     * <p>
     * 类型：自定义武器
     * 稀有度：RARE（稀有）
     * 堆叠数量：1
     * 功能：具有3发点射功能的特殊步枪
     * </p>
     */
    public static final RegistryObject<Item> PERFECT_RETROGRADE = ITEMS.register("perfect_retrograde",
            () -> new PerfectRetrogradeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    
    /**
     * 完美逆行步枪弹药
     * <p>
     * 类型：普通物品
     * 功能：配合完美逆行步枪使用的弹药
     * </p>
     */
    public static final RegistryObject<Item> PERFECT_RETROGRADE_AMMO = ITEMS.register("perfect_retrograde_ammo",
            () -> new Item(new Item.Properties()));

    // ==================== 技能与机灵 ====================
    /**
     * 太阳能手雷
     * <p>
     * 类型：技能物品
     * 堆叠数量：16
     * 功能：用于触发太阳能手雷技能
     * </p>
     */
    public static final RegistryObject<Item> SOLAR_GRENADE = ITEMS.register("solar_grenade",
            () -> new Item(new Item.Properties().stacksTo(16)));
    
    /**
     * 通用机灵
     * <p>
     * 类型：自定义物品（GhostItem）
     * 堆叠数量：1
     * 功能：右键使用打开 Ghost 界面，查看角色状态和技能
     * </p>
     */
    public static final RegistryObject<Item> GHOST_GENERAL = ITEMS.register("ghost_general",
            () -> new GhostItem(new Item.Properties().stacksTo(1)));
    
    /**
     * 强能裂隙职业技能
     * <p>
     * 类型：自定义物品（WellOfRadianceItem）
     * 稀有度：EPIC（史诗）
     * 堆叠数量：1
     * 功能：用于触发强能裂隙技能，按下 V 键施放
     * </p>
     */
    public static final RegistryObject<Item> WELL_OF_RADIANCE = ITEMS.register("well_of_radiance",
            () -> new WellOfRadianceItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // ==================== 异域装备 ====================
    /**
     * 太阳火胸甲
     * <p>
     * 类型：胸甲
     * 材料：下界合金
     * 稀有度：EPIC（史诗）
     * 功能：提供防护并增强太阳能技能效果
     * </p>
     */
    public static final RegistryObject<Item> SUNFIRE_CHESTPLATE = ITEMS.register("sunfire_chestplate",
            () -> new DestinyArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC)));
    
    /**
     * 点火护腿
     * <p>
     * 类型：护腿
     * 材料：下界合金
     * 稀有度：EPIC（史诗）
     * 功能：增强燃烧效果和移动速度
     * </p>
     */
    public static final RegistryObject<Item> IGNITION_LEGGINGS = ITEMS.register("ignition_leggings",
            () -> new DestinyArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC)));
    
    /**
     * 超载头盔
     * <p>
     * 类型：头盔
     * 材料：下界合金
     * 稀有度：EPIC（史诗）
     * 核心功能：佩戴时在10秒内击杀5个敌人可激活超载状态，重置所有技能冷却
     * </p>
     */
    public static final RegistryObject<Item> OVERLOAD_HELMET = ITEMS.register("overload_helmet",
            () -> new DestinyArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC)));

    // ==================== 资源物品 ====================
    /**
     * 微光
     * <p>
     * 类型：货币
     * 功能：Destiny 2中的通用货币，用于购买和升级装备
     * </p>
     */
    public static final RegistryObject<Item> GLIMMER = ITEMS.register("glimmer", () -> new Item(new Item.Properties()));
    
    /**
     * 异域宝箱
     * <p>
     * 类型：特殊物品
     * 稀有度：EPIC（史诗）
     * 功能：可开出稀有装备
     * </p>
     */
    public static final RegistryObject<Item> EXOTIC_ENGRAM = ITEMS.register("exotic_engram", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    
    /**
     * 奇怪硬币
     * <p>
     * 类型：特殊货币
     * 功能：用于购买特殊物品和装备
     * </p>
     */
    public static final RegistryObject<Item> STRANGE_COIN = ITEMS.register("strange_coin", () -> new Item(new Item.Properties()));

    /**
     * 注册物品
     * <p>
     * 将所有物品注册到事件总线上，确保在正确的时机进行注册
     * </p>
     * 
     * @param eventBus 事件总线实例
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}