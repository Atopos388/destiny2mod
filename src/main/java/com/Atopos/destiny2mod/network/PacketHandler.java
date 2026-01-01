package com.Atopos.destiny2mod.network;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.network.packet.PacketSolarSnap;
import com.Atopos.destiny2mod.network.packet.PacketThrowGrenade;
import com.Atopos.destiny2mod.network.packet.PacketWellOfRadiance;
import com.Atopos.destiny2mod.network.packet.S2CSyncOverloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络数据包处理器类
 * <p>
 * 负责注册和管理所有客户端与服务器之间的网络数据包
 * 提供统一的数据包通信通道，确保客户端和服务器之间的数据同步
 * </p>
 */
public class PacketHandler {
    /**
     * 协议版本号
     * <p>
     * 用于确保客户端和服务器使用相同版本的数据包格式
     * 如果版本不匹配，连接将被拒绝
     * </p>
     */
    private static final String PROTOCOL_VERSION = "1";
    
    /**
     * 主网络通信通道实例
     * <p>
     * 创建一个新的网络通道，用于处理所有数据包通信
     * 使用模组的MODID作为通道标识，确保唯一性
     * </p>
     */
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Destiny2Mod.MODID, "main"),
            // 供应商：返回当前协议版本
            () -> PROTOCOL_VERSION,
            // 客户端可接受的版本检查：客户端版本是否与当前版本匹配
            s -> true,
            // 服务器可接受的版本检查：服务器版本是否与当前版本匹配
            s -> true
    );

    /**
     * 注册所有网络数据包
     * <p>
     * 在这个方法中，所有自定义的网络数据包都被注册到网络通道中
     * 每个数据包都分配一个唯一的ID，用于在网络传输中识别不同类型的数据包
     * </p>
     */
    public static void register() {
        // 数据包ID计数器，从0开始递增
        int id = 0;
        
        // 注册手榴弹投掷数据包
        INSTANCE.registerMessage(id++, PacketThrowGrenade.class, 
                PacketThrowGrenade::encode, PacketThrowGrenade::decode, PacketThrowGrenade::handle);
        
        // 注册太阳能快枪数据包
        INSTANCE.registerMessage(id++, PacketSolarSnap.class, 
                PacketSolarSnap::encode, PacketSolarSnap::decode, PacketSolarSnap::handle);
        
        // 注册强能裂隙数据包
        INSTANCE.registerMessage(id++, PacketWellOfRadiance.class, 
                PacketWellOfRadiance::encode, PacketWellOfRadiance::decode, PacketWellOfRadiance::handle);
        
        // 注册超载状态同步数据包
        // 必须注册这个数据包，否则UI无法获取超载状态数据
        INSTANCE.registerMessage(id++, S2CSyncOverloadPacket.class, 
                S2CSyncOverloadPacket::encode, S2CSyncOverloadPacket::decode, S2CSyncOverloadPacket::handle);
    }
}