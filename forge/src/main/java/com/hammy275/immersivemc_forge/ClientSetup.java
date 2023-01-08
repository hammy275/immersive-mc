package com.hammy275.immersivemc_forge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ClientSetup {

    public static void doClientSetup() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }
}
