package com.Atopos.destiny2mod.item.custom;

import com.Atopos.destiny2mod.client.gui.GhostScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class GhostItem extends Item {
    public GhostItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        // 只有在客户端才处理界面打开逻辑
        if (pLevel.isClientSide) {
            // 使用 DistExecutor 安全地运行客户端代码
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen());
        }

        return InteractionResultHolder.success(itemstack);
    }

    // 这是一个只在客户端存在的方法
    private void openScreen() {
        Minecraft.getInstance().setScreen(new GhostScreen());
    }
}