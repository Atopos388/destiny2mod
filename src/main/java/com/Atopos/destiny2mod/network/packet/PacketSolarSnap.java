package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarSnapProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketSolarSnap {
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            // 1. 检查是否穿着爆燃护腿
            boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());

            // 2. 确定发射数量：金装 10 发，基础 3 发
            int count = hasLeggings ? 10 : 3;
            float spread = hasLeggings ? 40f : 20f; // 散布角度
            float startAngle = -spread / 2;
            float step = spread / (count - 1);

            long snapId = player.level().getGameTime(); // 标记这一波响指的 ID

            // [关键] 必须通过循环发射多个实体
            for (int i = 0; i < count; i++) {
                SolarSnapProjectile projectile = new SolarSnapProjectile(player.level(), player);
                projectile.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());

                // 存入强化标记和波次 ID
                projectile.getPersistentData().putLong("SnapID", snapId);
                projectile.getPersistentData().putBoolean("Enhanced", hasLeggings);

                // 计算每个火球的偏移角度
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + startAngle + (i * step), 0.0F, 1.5F, 1.0F);
                player.level().addFreshEntity(projectile);
            }

            // 设置机灵（近战）的冷却
            player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), 100);
        });
        context.get().setPacketHandled(true);
    }
}