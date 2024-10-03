package com.hammy275.immersivemc.common.compat;

import com.hammy275.immersivemc.common.compat.util.CompatUtils;

public class TinkersConstruct {

    public static final Class<?> craftingStation =
            CompatUtils.getClazz("slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity");
    public static final CompatData compatData = new CompatData("Tinkers Construct's Crafting Station",
            (config, newVal) -> config.useTinkersConstructCraftingStationImmersive = newVal);
}
