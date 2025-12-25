package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.SolarGrenadeProjectile;
import com.Atopos.destiny2mod.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot; // [核心修复] 必须导入此项以识别装备槽位
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * 烈日手雷投掷包
 * 处理手雷的生成、冷却判定以及超载金装逻辑
 */
public class PacketThrowGrenade {
    public PacketThrowGrenade() {}

    // 网络编解码方法，用于 Forge 网络系统
    public static void encode(PacketThrowGrenade msg, FriendlyByteBuf buf) {}
    public static PacketThrowGrenade decode(FriendlyByteBuf buf) { return new PacketThrowGrenade(); }

    public static void handle(PacketThrowGrenade msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            CompoundTag nbt = player.getPersistentData();
            boolean isOverloaded = nbt.getBoolean("OverloadActive");

            // --- 1. 超载触发窗口逻辑 ---
            // 如果玩家戴着超载头盔且当前不在超载状态，使用技能会激活 10 秒的杀敌观察窗口
            if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                if (nbt.getInt("OverloadKillWindow") <= 0) {
                    nbt.putInt("OverloadKillWindow", 200); // 10秒计时
                    nbt.putInt("OverloadKillCount", 0);    // 重置窗口内击杀
                }
            }

            // --- 2. 基础投掷逻辑 ---
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);

            SolarGrenadeProjectile grenade = new SolarGrenadeProjectile(player.level(), player);
            grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(grenade);

            // --- 3. 超载惩罚与冷却逻辑 ---
            if (isOverloaded) {
                // 超载 Buff 激活期间：技能无 CD，但统计连续使用次数
                int usage = nbt.getInt("OverloadUsage") + 1;
                nbt.putInt("OverloadUsage", usage);

                // 连续施放 5 次技能后触发负面效果
                if (usage >= 5) {
                    player.hurt(player.damageSources().magic(), 4.0F); // 扣除 4 点生命 (2颗心)
                    nbt.putInt("OverloadUsage", 0);
                }
            } else {
                // 普通状态：正常给手雷添加冷却（200 ticks = 10秒）
                player.getCooldowns().addCooldown(ItemInit.SOLAR_GRENADE.get(), 200);
            }
        });
        context.setPacketHandled(true);
    }
}