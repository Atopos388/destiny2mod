package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarSnapProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketSolarSnap {
    public PacketSolarSnap() {}

    // 修复编译错误：添加编解码方法
    public static void encode(PacketSolarSnap msg, FriendlyByteBuf buf) {}
    public static PacketSolarSnap decode(FriendlyByteBuf buf) { return new PacketSolarSnap(); }

    public static void handle(PacketSolarSnap msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            CompoundTag nbt = player.getPersistentData();
            boolean isOverloaded = nbt.getBoolean("OverloadActive");

            // --- 1. 超载触发窗口逻辑 ---
            // 如果没在超载状态且带着头盔，使用技能开启10秒杀敌窗口
            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200); // 10秒 = 200 ticks
                    nbt.putInt("OverloadKillCount", 0);    // 重置窗口内击杀数
                }
            }

            // --- 2. 基础技能发射逻辑 ---
            boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());
            int count = hasLeggings ? 10 : 3;
            float spread = hasLeggings ? 45f : 20f;
            float startAngle = -spread / 2f;
            float step = spread / (count - 1);

            for (int i = 0; i < count; i++) {
                SolarSnapProjectile projectile = new SolarSnapProjectile(player.level(), player);
                projectile.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + startAngle + (i * step), 0.0F, 1.6F, 0.8F);
                player.level().addFreshEntity(projectile);
            }

            // --- 3. 超载惩罚与冷却逻辑 ---
            if (isOverloaded) {
                // 超载状态：无CD，但累加使用计数
                int usage = nbt.getInt("OverloadUsage") + 1;
                nbt.putInt("OverloadUsage", usage);
                if (usage >= 5) {
                    player.hurt(player.damageSources().magic(), 4.0F); // 扣2颗心
                    nbt.putInt("OverloadUsage", 0);
                }
            } else {
                // 普通状态：正常进入冷却
                player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), 100);
            }
        });
        context.setPacketHandled(true);
    }
}