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
    public static void encode(PacketSolarSnap msg, FriendlyByteBuf buf) {}
    public static PacketSolarSnap decode(FriendlyByteBuf buf) { return new PacketSolarSnap(); }

    public static void handle(PacketSolarSnap msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CompoundTag nbt = player.getPersistentData();
            boolean isOverloaded = nbt.getBoolean("OverloadActive");

            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200);
                    nbt.putInt("OverloadKillCount", 0);
                }
            }

            boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.IGNITION_LEGGINGS.get());
            int count = hasLeggings ? 10 : 3;
            float spread = 45f;
            float step = spread / (count - 1);
            for (int i = 0; i < count; i++) {
                SolarSnapProjectile p = new SolarSnapProjectile(player.level(), player);
                p.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
                p.shootFromRotation(player, player.getXRot(), player.getYRot() + (-spread/2f + (i*step)), 0.0F, 1.6F, 0.8F);
                player.level().addFreshEntity(p);
            }

            if (isOverloaded) {
                int use = nbt.getInt("OverloadUsage") + 1;
                nbt.putInt("OverloadUsage", use);
                if (use >= 5) {
                    player.hurt(player.damageSources().magic(), 4.0F);
                    nbt.putInt("OverloadUsage", 0);
                }
            } else player.getCooldowns().addCooldown(ItemInit.GHOST_GENERAL.get(), 100);
        });
        context.setPacketHandled(true);
    }
}