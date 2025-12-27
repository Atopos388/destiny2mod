package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarSnapProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketSolarSnap {
    public PacketSolarSnap() {}
    public static void encode(PacketSolarSnap msg, FriendlyByteBuf buf) {}
    public static PacketSolarSnap decode(FriendlyByteBuf buf) { return new PacketSolarSnap(); }

    public static void handle(PacketSolarSnap msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CompoundTag nbt = player.getPersistentData();
            boolean isOverloaded = nbt.getBoolean("OverloadActive");

            // 1. 超载窗口开启逻辑
            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200);
                    nbt.putInt("OverloadKillCount", 0);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                }
            }

            // 2. 发射火球 (爆燃护腿检测)
            boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());
            int count = hasLeggings ? 10 : 3;
            long bid = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                SolarSnapProjectile p = new SolarSnapProjectile(player.level(), player);
                p.setEnhanced(hasLeggings);
                p.setBatchId(bid);
                p.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
                p.shootFromRotation(player, player.getXRot(), player.getYRot() + (i*10 - (count*5)), 0.0F, 1.6F, 1.0F);
                player.level().addFreshEntity(p);
            }

            // 3. 超载判定与冷却逻辑
            if (isOverloaded) {
                // [修改逻辑] 0.8秒极速冷却 (16 ticks)
                player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), 16);

                int use = nbt.getInt("OverloadUsage") + 1;
                if (use >= 5) {
                    player.invulnerableTime = 0;
                    player.hurt(player.damageSources().magic(), 4.0F); // 扣除2颗心
                    nbt.putInt("OverloadUsage", 0);
                } else {
                    nbt.putInt("OverloadUsage", use);
                }
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
            } else {
                // 正常模式冷却：5秒
                player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), 100);
            }
        });
        context.setPacketHandled(true);
    }
}