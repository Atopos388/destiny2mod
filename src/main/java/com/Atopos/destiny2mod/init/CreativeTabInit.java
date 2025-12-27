package com.Atopos.destiny2mod.init;

import com.Atopos.destiny2mod.Destiny2Mod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Destiny2Mod.MODID);

    public static final RegistryObject<CreativeModeTab> DESTINY_TAB = CREATIVE_MODE_TABS.register("destiny_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemInit.GLIMMER.get()))
                    .title(Component.translatable("creativetab.destiny_tab"))
                    .displayItems((parameters, output) -> {
                        // 基础货币与资源
                        output.accept(ItemInit.GLIMMER.get());
                        output.accept(ItemInit.STRANGE_COIN.get());
                        output.accept(ItemInit.EXOTIC_ENGRAM.get());

                        // 武器与技能
                        if (ItemInit.PERFECT_RETROGRADE.isPresent()) output.accept(ItemInit.PERFECT_RETROGRADE.get());
                        output.accept(ItemInit.SOLAR_GRENADE.get());
                        output.accept(ItemInit.GHOST_GENERAL.get());

                        // 异域防具 (烈日 3.0 套装)
                        output.accept(ItemInit.OVERLOAD_HELMET.get());    // 超载头盔
                        output.accept(ItemInit.SUNFIRE_CHESTPLATE.get()); // 炎阳护甲
                        output.accept(ItemInit.IGNITION_LEGGINGS.get()); // 爆燃护腿
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}