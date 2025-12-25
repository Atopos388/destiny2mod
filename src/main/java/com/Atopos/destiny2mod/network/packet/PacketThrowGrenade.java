package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarGrenadeProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketThrowGrenade {
    public PacketThrowGrenade() {}

    public PacketThrowGrenade(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public static PacketThrowGrenade decode(FriendlyByteBuf buf) {
        return new PacketThrowGrenade(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // 播放音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);

                // 创建并投掷手雷实体
                SolarGrenadeProjectile grenade = new SolarGrenadeProjectile(player.level(), player);
                grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                player.level().addFreshEntity(grenade);

                // [关键修复] 设置手雷物品的冷却，而不是手持物品的冷却
                // 这样 HUD 界面才能正确监视到冷却进度
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 200);
            }
        });
        context.get().setPacketHandled(true);
    }
}