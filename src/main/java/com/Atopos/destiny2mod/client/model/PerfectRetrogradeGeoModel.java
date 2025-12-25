package com.Atopos.destiny2mod.client.model;

import com.Atopos.destiny2mod.Destiny2Mod;
import com.Atopos.destiny2mod.item.custom.PerfectRetrogradeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PerfectRetrogradeGeoModel extends GeoModel<PerfectRetrogradeItem> {
    @Override
    public ResourceLocation getModelResource(PerfectRetrogradeItem animatable) {
        return new ResourceLocation(Destiny2Mod.MODID, "geo/perfect_retrograde.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PerfectRetrogradeItem animatable) {
        // 指向: assets/destiny2mod/textures/item/perfect_retrograde.png
        return new ResourceLocation(Destiny2Mod.MODID, "textures/item/perfect_retrograde.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PerfectRetrogradeItem animatable) {
        return new ResourceLocation(Destiny2Mod.MODID, "animations/perfect_retrograde.animation.json");
    }
}