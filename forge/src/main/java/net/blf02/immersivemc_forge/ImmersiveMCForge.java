package net.blf02.immersivemc_forge;

import dev.architectury.platform.forge.EventBuses;
import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.client.config.screen.ConfigScreen;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {
    public ImmersiveMCForge() {
        EventBuses.registerModEventBus(ImmersiveMC.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ImmersiveMC.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            doClientSetup();
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
    }

    private void doClientSetup() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }
}
