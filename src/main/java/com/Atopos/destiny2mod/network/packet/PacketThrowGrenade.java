package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarGrenadeProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketThrowGrenade {
    public PacketThrowGrenade() {}
    public static void encode(PacketThrowGrenade msg, FriendlyByteBuf buf) {}
    public static PacketThrowGrenade decode(FriendlyByteBuf buf) { return new PacketThrowGrenade(); }

    public static void handle(PacketThrowGrenade msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CompoundTag nbt = player.getPersistentData();
            boolean isOverloaded = nbt.getBoolean("OverloadActive");

            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                }
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);

            SolarGrenadeProjectile g = new SolarGrenadeProjectile(player.level(), player);
            g.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(g);

            if (isOverloaded) {
                // [修改逻辑] 0.8秒极速冷却 (16 ticks)
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 16);

                int use = nbt.getInt("OverloadUsage") + 1;
                if (use >= 5) {
                    player.invulnerableTime = 0;
                    player.hurt(player.damageSources().magic(), 4.0F);
                    nbt.putInt("OverloadUsage", 0);
                } else {
                    nbt.putInt("OverloadUsage", use);
                }
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
            } else {
                // 正常模式冷却：10秒
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 200);
            }
        });
        context.setPacketHandled(true);
    }
}