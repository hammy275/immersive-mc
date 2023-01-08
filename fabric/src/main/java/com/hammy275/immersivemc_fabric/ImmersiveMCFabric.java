package com.hammy275.immersivemc_fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ImmersiveMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ImmersiveMC.init();
        ModLoadingContext.registerConfig(ImmersiveMC.MOD_ID, ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
        try {
            Class.forName("net.blf02.vrapi.api.IVRAPI");
            VRPlugin.initVR();
        } catch (ClassNotFoundException e) {
            ImmersiveMC.LOGGER.info("Not loading with mc-vr-api; it wasn't found!");
        }
    }
}
