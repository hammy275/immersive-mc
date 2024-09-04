package com.hammy275.immersivemc_forge;

import com.hammy275.immersivemc.ImmersiveMC;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {
    public ImmersiveMCForge() {
        EventBuses.registerModEventBus(ImmersiveMC.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        ImmersiveMC.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup();
        }
    }


}
