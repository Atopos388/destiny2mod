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
 * 模组主类
 * 修复：修正了包名路径并确保 onLoadComplete 正确调用 ModDiagnostics.runSystemCheck()
 */
@Mod(Destiny2Mod.MODID)
public class Destiny2Mod {
    public static final String MODID = "destiny2mod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Destiny2Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册初始化项
        ItemInit.register(modEventBus);
        CreativeTabInit.register(modEventBus);
        EntityInit.register(modEventBus);

        // 监听通用设置事件
        modEventBus.addListener(this::commonSetup);
        // 监听加载完成事件
        modEventBus.addListener(this::onLoadComplete);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        // [修复] 此处调用的方法名必须与 ModDiagnostics.java 中的 runSystemCheck 一致
        ModDiagnostics.runSystemCheck();
    }
}