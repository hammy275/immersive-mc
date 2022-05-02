package net.blf02.immersivemc.client.config;


import net.blf02.immersivemc.client.config.screen.ConfigScreen;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

public class ClientInit {
    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, screen) -> new ConfigScreen(screen));
    }
}
