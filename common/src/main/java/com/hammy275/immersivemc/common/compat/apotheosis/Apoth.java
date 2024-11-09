package com.hammy275.immersivemc.common.compat.apotheosis;

import com.hammy275.immersivemc.common.compat.CompatData;

public class Apoth {

    public static ApothCompat apothImpl = new ApothNullImpl();
    public static CompatData compatData = new CompatData("Apotheosis",
            (config, newValue) -> {
                config.useApotheosisEnchantmentTableImmersive = false;
                config.useApotheosisSalvagingTableImmersive = false;
                apothImpl = new ApothNullImpl();
            });
}
