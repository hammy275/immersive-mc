package net.blf02.forge;

import dev.architectury.platform.forge.EventBuses;
import net.blf02.immersivemc.ImmersiveMC;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {
    public ImmersiveMCForge() {
        EventBuses.registerModEventBus(ImmersiveMC.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ImmersiveMC.init();
    }
}
