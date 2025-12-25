package com.Atopos.destiny2mod.client.renderer.item;

import com.Atopos.destiny2mod.client.model.PerfectRetrogradeGeoModel;
import com.Atopos.destiny2mod.item.custom.PerfectRetrogradeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PerfectRetrogradeItemRenderer extends GeoItemRenderer<PerfectRetrogradeItem> {
    public PerfectRetrogradeItemRenderer() {
        super(new PerfectRetrogradeGeoModel());
    }
}