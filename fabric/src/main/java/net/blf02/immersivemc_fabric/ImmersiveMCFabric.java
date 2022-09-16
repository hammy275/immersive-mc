package net.blf02.immersivemc_fabric;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ImmersiveMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ImmersiveMC.init();
        ModLoadingContext.registerConfig(ImmersiveMC.MOD_ID, ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
    }
}
