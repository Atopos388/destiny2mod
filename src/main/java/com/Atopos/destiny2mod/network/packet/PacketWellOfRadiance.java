package com.Atopos.destiny2mod.network.packet;

import com.Atopos.destiny2mod.entity.custom.WellOfRadianceEntity;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 强能裂隙网络数据包类
 * <p>
 * 负责处理强能裂隙技能的客户端-服务器通信
 * 当玩家按下 V 键时，客户端发送此数据包到服务器
 * 服务器接收到数据包后，执行强能裂隙的生成和冷却逻辑
 * </p>
 */
public class PacketWellOfRadiance {
    /**
     * 默认构造函数
     * <p>
     * 创建一个新的强能裂隙数据包实例
     * 由于此数据包不需要携带额外数据，构造函数为空
     * </p>
     */
    public PacketWellOfRadiance() {}
    
    /**
     * 编码数据包
     * <p>
     * 将数据包编码为 FriendlyByteBuf
     * 由于此数据包不需要携带额外数据，编码方法为空
     * </p>
     * 
     * @param msg 要编码的数据包
     * @param buf 目标字节缓冲区
     */
    public static void encode(PacketWellOfRadiance msg, FriendlyByteBuf buf) {
        // 即使为空，也写入一个字节以确保数据包不为空
        buf.writeBoolean(true);
    }
    
    /**
     * 解码数据包
     * <p>
     * 从 FriendlyByteBuf 解码出强能裂隙数据包
     * 由于此数据包不需要携带额外数据，直接返回新实例
     * </p>
     * 
     * @param buf 包含数据包数据的字节缓冲区
     * @return 解码后的数据包实例
     */
    public static PacketWellOfRadiance decode(FriendlyByteBuf buf) {
        // 读取占位字节
        if (buf.isReadable()) {
            buf.readBoolean();
        }
        return new PacketWellOfRadiance();
    }
    
    /**
     * 处理数据包
     * <p>
     * 在服务器端处理强能裂隙数据包
     * 执行以下逻辑：
     * 1. 检查玩家是否戴着超载头盔，尝试开启杀敌窗口
     * 2. 播放强能裂隙生成音效
     * 3. 生成强能裂隙实体
     * 4. 处理技能冷却时间
     * 5. 处理超载模式的特殊逻辑
     * </p>
     * 
     * @param msg 接收到的数据包
     * @param contextSupplier 网络上下文供应器
     */
    public static void handle(PacketWellOfRadiance msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // 在工作队列中执行，确保线程安全
        context.enqueueWork(() -> {
            try {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                
                Level level = player.level();
                CompoundTag nbt = player.getPersistentData();
                // 检查玩家是否处于超载状态
                boolean isOverloaded = nbt.getBoolean("OverloadActive");
                
                // 1. 如果未激活超载且戴着超载头盔，尝试开启杀敌窗口
                if (!isOverloaded && player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.OVERLOAD_HELMET.get())) {
                    if (nbt.getInt("OverloadKillWindow") <= 0) {
                        // 开启200游戏刻（10秒）的杀敌窗口
                        nbt.putInt("OverloadKillWindow", 200);
                        nbt.putInt("OverloadKillCount", 0);
                        // 同步数据到客户端
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                    }
                }
                
                // 2. 播放强能裂隙生成音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.8F);
                
                // 3. 生成强能裂隙实体
                WellOfRadianceEntity well = new WellOfRadianceEntity(level, player);
                // 修复：确保实体位置正确
                well.setPos(player.getX(), player.getY(), player.getZ());
                
                // 修复：使用 addFreshEntity 确保实体同步到客户端
                level.addFreshEntity(well);
                
                // 4. 超载模式逻辑处理
                if (isOverloaded) {
                    // 超载模式：1.2秒冷却 (24游戏刻)
                    player.getCooldowns().addCooldown(ItemInit.WELL_OF_RADIANCE.get(), 24);
                    
                    // 累加超载模式下的技能使用次数
                    int use = nbt.getInt("OverloadUsage") + 1;
                    nbt.putInt("OverloadUsage", use);
                    
                    // 从第5次开始，每一次施法都会扣血（4点伤害 = 2颗心）
                    if (use >= 5) {
                        player.invulnerableTime = 0; // 重置无敌帧，确保扣血生效
                        player.hurt(player.damageSources().magic(), 4.0F);
                    }
                    
                    // 同步数据到客户端
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncOverloadPacket(nbt));
                } else {
                    // 正常模式：60秒冷却 (1200游戏刻)
                    player.getCooldowns().addCooldown(ItemInit.WELL_OF_RADIANCE.get(), 1200);
                }
            } catch (Exception e) {
                // 仅在开发环境打印堆栈，或使用 Logger 记录错误
                e.printStackTrace();
            }
        });
        // 标记数据包已处理
        context.setPacketHandled(true);
    }
}