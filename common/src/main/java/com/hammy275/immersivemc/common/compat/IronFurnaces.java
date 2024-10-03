package com.hammy275.immersivemc.common.compat;

import com.hammy275.immersivemc.common.compat.util.CompatUtils;

public class IronFurnaces {

    public static final Class<?> ironFurnaceTileBase =
            CompatUtils.getClazz("ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase");
    public static final CompatData compatData = new CompatData("Iron Furnaces' Furnaces",
            (config, newVal) -> config.useIronFurnacesFurnaceImmersive = newVal);


}
