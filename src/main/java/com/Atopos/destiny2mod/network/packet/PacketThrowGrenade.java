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

            // 1. 如果未激活超载且戴着超载头盔，尝试开启杀敌窗口
            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200);
                    nbt.putInt("OverloadKillCount", 0);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                }
            }

            // 2. 播放音效并生成手雷实体
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);

            SolarGrenadeProjectile g = new SolarGrenadeProjectile(player.level(), player);
            g.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(g);

            // 3. 超载模式逻辑处理
            if (isOverloaded) {
                // 超载模式：0.8秒极速冷却 (16 ticks)
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 16);

                // 累加使用次数
                int use = nbt.getInt("OverloadUsage") + 1;
                nbt.putInt("OverloadUsage", use);

                // [核心修复] 逻辑调整：从第 5 次开始，每一次施法都会扣血
                if (use >= 5) {
                    player.invulnerableTime = 0; // 强制重置无敌帧，确保连续扣血生效
                    player.hurt(player.damageSources().magic(), 4.0F); // 扣除 4 点伤害（2颗心）
                    // 注意：此处不再重置 OverloadUsage 为 0，使其保持在 >= 5 的状态直至超载结束
                }

                // 同步数据到客户端（用于 UI 潜在的计数显示或同步）
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
            } else {
                // 正常模式：40秒冷却 (800 ticks)
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 800);
            }
        });
        context.setPacketHandled(true);
    }
}