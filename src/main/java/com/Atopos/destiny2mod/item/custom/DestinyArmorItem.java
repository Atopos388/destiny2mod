package com.Atopos.destiny2mod.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DestinyArmorItem extends ArmorItem {

    public DestinyArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        // 手动检查 canEquip，因为 vanilla ArmorItem#use 可能在某些 Forge 版本中不会正确处理该检查或反馈
        if (!this.canEquip(itemstack, this.getEquipmentSlot(), player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.destiny2mod.exotic_limit"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        // 1. 基础检查：确保槽位正确
        if (!super.canEquip(stack, armorType, entity)) {
            return false;
        }

        // 2. 异域装备唯一性检查
        if (entity instanceof Player player) {
            // 遍历玩家当前的所有盔甲槽位
            for (ItemStack equipped : player.getArmorSlots()) {
                // 如果发现已装备了其他的 DestinyArmorItem（异域装备）
                // 且该装备不是当前正在尝试装备的这件（避免自身冲突逻辑，虽通常不会发生）
                if (equipped.getItem() instanceof DestinyArmorItem && equipped != stack) {
                    // 检查是否是同一槽位的替换（如果是替换同槽位的旧异域，理应允许？但 Minecraft 的 canEquip 发生在装备动作之前）
                    // 如果玩家试图用一个异域头盔替换另一个异域头盔，此时 equipped 是旧头盔。
                    // 但通常 canEquip 是针对“能否放入该槽位”。
                    // 如果槽位里已经有东西，通常是先拿下来再放上去，或者是交换。
                    // 无论如何，为了严格限制“同时只能存在一件”，如果身上已经有一件（且不是当前要装的这个位置的空缺状态），则禁止。
                    
                    // 细化逻辑：
                    // 如果我正在装备头盔，我需要检查 胸甲、护腿、靴子 是否有异域。
                    // 如果我正在装备头盔，而头盔槽已经有一个异域头盔：
                    //   - 如果是右键替换，canEquip 返回 false 会导致无法替换。这符合“不能同时存在”的严格定义（虽然替换后其实只剩一件）。
                    //   - 用户说“怪怪的”，可能就是这种替换逻辑的问题。
                    //   - 但如果我返回 false，玩家必须先卸下旧的，再装新的。这是一种“笨拙但稳定”的方法。
                    //   - 相比于“自动卸下并掉落”，这种方式更可控。
                    
                    // 我们应该只检查“其他”槽位
                    // 获取当前已装备物品的槽位
                    EquipmentSlot equippedSlot = getEquipmentSlotForItem(equipped);
                    
                    if (equippedSlot != armorType) {
                        // 如果在其他槽位发现了异域装备，则禁止装备当前物品
                        // 发送一条提示消息给玩家（可选，但 canEquip 频繁调用，小心刷屏）
                        // 由于 canEquip 在客户端和服务端都会调用，且可能每帧调用（用于渲染），不能在这里发消息。
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    // 辅助方法：获取物品对应的槽位
    private EquipmentSlot getEquipmentSlotForItem(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getEquipmentSlot();
        }
        return EquipmentSlot.MAINHAND; // Fallback
    }
}
