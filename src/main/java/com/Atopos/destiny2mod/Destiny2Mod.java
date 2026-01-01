package com.Atopos.destiny2mod;

import com.Atopos.destiny2mod.init.CreativeTabInit;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.Atopos.destiny2mod.util.ModDiagnostics;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Destiny2Mod 模组主类
 * <p>
 * 这是整个模组的入口点，负责初始化模组的各个组件
 * 并注册事件监听器。
 * </p>
 * <p>
 * 修复：修正了包名路径并确保 onLoadComplete 正确调用 ModDiagnostics.runSystemCheck()
 * </p>
 */
@Mod(Destiny2Mod.MODID)
public class Destiny2Mod {
    /**
     * 模组的唯一标识符
     * 必须与 mods.toml 中的 modId 一致
     */
    public static final String MODID = "destiny2mod";
    
    /**
     * 模组的日志记录器
     * 用于输出调试和错误信息
     */
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 模组的构造函数
     * 负责初始化模组的各个组件和注册事件监听器
     */
    public Destiny2Mod() {
        // 获取模组事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册初始化项
        ItemInit.register(modEventBus);          // 注册物品
        CreativeTabInit.register(modEventBus);   // 注册创造模式标签
        EntityInit.register(modEventBus);        // 注册实体

        // 监听通用设置事件
        modEventBus.addListener(this::commonSetup);
        // 监听加载完成事件
        modEventBus.addListener(this::onLoadComplete);

        // 注册到 MinecraftForge 事件总线
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 通用设置事件处理方法
     * <p>
     * 在模组加载过程中调用，用于执行需要在主线程中运行的初始化操作
     * </p>
     * 
     * @param event FMLCommonSetupEvent 事件对象
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 在工作队列中注册数据包处理器
        event.enqueueWork(() -> {
            PacketHandler.register();
        });
    }

    /**
     * 加载完成事件处理方法
     * <p>
     * 在模组完全加载后调用，用于执行最终的初始化操作
     * </p>
     * 
     * @param event FMLLoadCompleteEvent 事件对象
     */
    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        // 运行系统检查，确保模组各组件正常工作
        ModDiagnostics.runSystemCheck();
    }
}