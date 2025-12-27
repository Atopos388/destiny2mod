package com.Atopos.destiny2mod.network;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import com.Atopos.destiny2mod.network.packet.S2CSyncOverloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Destiny2Mod.MODID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, PacketThrowGrenade.class, PacketThrowGrenade::encode, PacketThrowGrenade::decode, PacketThrowGrenade::handle);
        INSTANCE.registerMessage(id++, PacketSolarSnap.class, PacketSolarSnap::encode, PacketSolarSnap::decode, PacketSolarSnap::handle);
        // [新增] 必须注册这个，否则 UI 没数据
        INSTANCE.registerMessage(id++, S2CSyncOverloadPacket.class, S2CSyncOverloadPacket::encode, S2CSyncOverloadPacket::decode, S2CSyncOverloadPacket::handle);
    }
}