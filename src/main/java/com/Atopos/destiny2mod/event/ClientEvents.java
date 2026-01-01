package com.Atopos.destiny2mod.event;

import com.Atopos.destiny2mod.Destiny2Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 客户端事件处理类
 * <p>
 * 处理仅在客户端执行的事件逻辑，例如UI提示、渲染等
 * </p>
 */
@Mod.EventBusSubscriber(modid = Destiny2Mod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * 物品提示事件监听器
     * <p>
     * 为模组物品添加自定义的描述文本（Flavor Text）
     * 自动查找 item.modid.name.desc 格式的语言键
     * 并以灰色斜体（命运2风格）显示
     * </p>
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        // 检查物品是否属于本模组
        // 使用 ForgeRegistries 获取物品的 Registry Name
        if (stack.getItem() != null && ForgeRegistries.ITEMS.getKey(stack.getItem()) != null &&
            ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals(Destiny2Mod.MODID)) {
            
            // 构建描述键：item.destiny2mod.item_name.desc
            // 注意：getDescriptionId() 返回 item.destiny2mod.item_name
            String key = stack.getDescriptionId() + ".desc";
            
            // 检查语言文件中是否存在该键
            if (net.minecraft.client.resources.language.I18n.exists(key)) {
                // 添加灰色斜体描述
                event.getToolTip().add(Component.translatable(key)
                        .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
            }
        }
    }
}
