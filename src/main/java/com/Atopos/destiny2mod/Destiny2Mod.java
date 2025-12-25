package com.Atopos.destiny2mod;

import com.Atopos.destiny2mod.init.CreativeTabInit;
import com.Atopos.destiny2mod.init.EntityInit;
import com.Atopos.destiny2mod.init.ItemInit;
import com.Atopos.destiny2mod.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(Destiny2Mod.MODID)
public class Destiny2Mod {
    public static final String MODID = "destiny2mod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Destiny2Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // [重要] 初始化 GeckoLib 环境
        GeckoLib.initialize();

        ItemInit.register(modEventBus);
        CreativeTabInit.register(modEventBus);
        EntityInit.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }
}