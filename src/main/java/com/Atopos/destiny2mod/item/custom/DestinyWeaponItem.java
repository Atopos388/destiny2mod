package com.Atopos.destiny2mod.item.custom;

import com.Atopos.destiny2mod.item.perk.Perk;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DestinyWeaponItem extends Item {

    public DestinyWeaponItem(Properties pProperties) {
        super(pProperties);
    }

    // ... (appendHoverText 方法保持不变) ...
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (pStack.hasTag() && pStack.getTag().contains("Perks")) {
            pTooltipComponents.add(Component.literal("§7[ 武器配置 ]"));
            ListTag perksList = pStack.getTag().getList("Perks", 8);
            for (int i = 0; i < perksList.size(); i++) {
                try {
                    Perk perk = Perk.valueOf(perksList.getString(i));
                    pTooltipComponents.add(Component.literal(" §8[" + perk.getCategory().getName() + "] ")
                            .append(perk.getColoredName()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    // === 新增：允许子类定义专属词条池 ===
    // 返回一个 Map，Key是槽位(0-3)，Value是该槽位允许出现的词条列表
    // 如果返回 null，则使用默认的全随机逻辑
    public Map<Integer, List<Perk>> getPerkPool() {
        return null;
    }

    // === 修改后的随机逻辑 ===
    public static void randomizePerks(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains("Perks")) return;

        ListTag perksList = new ListTag();
        Random random = new Random();

        // 检查这把枪是否有自定义池
        Map<Integer, List<Perk>> customPool = null;
        if (stack.getItem() instanceof DestinyWeaponItem weapon) {
            customPool = weapon.getPerkPool();
        }

        // 1. 枪管 (Slot 0)
        perksList.add(StringTag.valueOf(pickPerk(customPool, 0, Perk.Category.BARREL, random).name()));
        // 2. 弹夹 (Slot 1)
        perksList.add(StringTag.valueOf(pickPerk(customPool, 1, Perk.Category.MAGAZINE, random).name()));
        // 3. 特性一 (Slot 2)
        perksList.add(StringTag.valueOf(pickPerk(customPool, 2, Perk.Category.TRAIT_1, random).name()));
        // 4. 特性二 (Slot 3) - 这里通常放高爆载荷/蜻蜓
        perksList.add(StringTag.valueOf(pickPerk(customPool, 3, Perk.Category.TRAIT_2, random).name()));

        nbt.put("Perks", perksList);
        stack.setTag(nbt);
    }

    // 辅助方法：从自定义池或默认池中选一个
    private static Perk pickPerk(Map<Integer, List<Perk>> customPool, int slotIndex, Perk.Category category, Random random) {
        // 1. 尝试从自定义池取
        if (customPool != null && customPool.containsKey(slotIndex)) {
            List<Perk> pool = customPool.get(slotIndex);
            if (pool != null && !pool.isEmpty()) {
                return pool.get(random.nextInt(pool.size()));
            }
        }

        // 2. 如果没有自定义池，从全局默认池取 (Fallback)
        return getRandomPerkByCategory(category, random);
    }

    private static Perk getRandomPerkByCategory(Perk.Category category, Random random) {
        List<Perk> pool = new ArrayList<>();
        for (Perk p : Perk.values()) {
            if (p.getCategory() == category) pool.add(p);
        }
        if (pool.isEmpty()) return Perk.FULL_BORE;
        return pool.get(random.nextInt(pool.size()));
    }

    // ... (getTotalDamageMultiplier 等工具方法保持不变) ...
    public static float getTotalDamageMultiplier(ItemStack stack) {
        float mult = 1.0f;
        List<Perk> perks = getPerks(stack);
        for (Perk p : perks) mult *= p.getDamageMultiplier();
        return mult;
    }

    public static float getTotalSpeedMultiplier(ItemStack stack) {
        float mult = 1.0f;
        List<Perk> perks = getPerks(stack);
        for (Perk p : perks) mult *= p.getSpeedMultiplier();
        return mult;
    }

    public static float getTotalFireDelayMultiplier(ItemStack stack, @Nullable net.minecraft.world.entity.LivingEntity entity) {
        float mult = 1.0f;
        List<Perk> perks = getPerks(stack);
        for (Perk p : perks) mult *= p.getFireDelayMultiplier();
        
        // 检查实体是否有挖掘速度提升效果（急迫），如果有，则视为在强能裂隙中，减少开火冷却
        if (entity != null && entity.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)) {
            // 急迫 II 提供了 20% 的挖掘速度提升 (每级 0.2)
            // 这里我们将其转换为开火冷却减少，例如减少 30% 的冷却时间 (变为原来的 0.7)
            mult *= 0.7f;
        }
        
        return mult;
    }

    /**
     * @deprecated 使用带有 entity 参数的版本 {@link #getTotalFireDelayMultiplier(ItemStack, net.minecraft.world.entity.LivingEntity)} 以支持强能裂隙增益
     */
    @Deprecated
    public static float getTotalFireDelayMultiplier(ItemStack stack) {
        return getTotalFireDelayMultiplier(stack, null);
    }

    private static List<Perk> getPerks(ItemStack stack) {
        List<Perk> list = new ArrayList<>();
        if (stack.hasTag() && stack.getTag().contains("Perks")) {
            ListTag tags = stack.getTag().getList("Perks", 8);
            for (int i = 0; i < tags.size(); i++) {
                try { list.add(Perk.valueOf(tags.getString(i))); } catch (Exception e) {}
            }
        }
        return list;
    }
}