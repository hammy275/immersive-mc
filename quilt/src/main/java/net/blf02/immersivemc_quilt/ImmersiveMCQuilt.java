package net.blf02.immersivemc_quilt;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ImmersiveMCQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        ImmersiveMC.init();
        ModLoadingContext.registerConfig(ImmersiveMC.MOD_ID, ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
        VRPlugin.initVR();
    }
}
