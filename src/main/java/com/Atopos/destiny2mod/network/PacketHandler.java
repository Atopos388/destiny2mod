package com.Atopos.destiny2mod.network;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    // [修复] 使用单参数 ResourceLocation 并确保格式为 "modid:path"
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Destiny2Mod.MODID + ":main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        // 注册 G 键手雷包
        INSTANCE.registerMessage(id++, PacketThrowGrenade.class,
                PacketThrowGrenade::encode,
                PacketThrowGrenade::decode,
                PacketThrowGrenade::handle);

        // [关键] 注册 C 键近战包
        INSTANCE.registerMessage(id++, PacketSolarSnap.class,
                PacketSolarSnap::encode,
                PacketSolarSnap::decode,
                PacketSolarSnap::handle);
    }
}