package com.Atopos.destiny2mod.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CSyncOverloadPacket {
    private final CompoundTag data;

    public S2CSyncOverloadPacket(CompoundTag data) { this.data = data; }

    public static void encode(S2CSyncOverloadPacket msg, FriendlyByteBuf buf) { buf.writeNbt(msg.data); }
    public static S2CSyncOverloadPacket decode(FriendlyByteBuf buf) { return new S2CSyncOverloadPacket(buf.readNbt()); }

    public static void handle(S2CSyncOverloadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 将服务端发来的超载数据写入客户端玩家 NBT，激活 UI 显示
            if (Minecraft.getInstance().player != null) {
                CompoundTag target = Minecraft.getInstance().player.getPersistentData();
                target.putBoolean("OverloadActive", msg.data.getBoolean("OverloadActive"));
                target.putInt("OverloadBuffTimer", msg.data.getInt("OverloadBuffTimer"));
                target.putInt("OverloadKillWindow", msg.data.getInt("OverloadKillWindow"));
                target.putInt("OverloadKillCount", msg.data.getInt("OverloadKillCount"));
                target.putInt("OverloadUsage", msg.data.getInt("OverloadUsage"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}