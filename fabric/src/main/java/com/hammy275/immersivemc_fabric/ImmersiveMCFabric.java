package com.hammy275.immersivemc_fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.Lootr;
import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;

public class ImmersiveMCFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ImmersiveMC.init();
        try {
            Class.forName("net.blf02.vrapi.api.IVRAPI");
            VRPlugin.initVR();
        } catch (ClassNotFoundException e) {
            ImmersiveMC.LOGGER.info("Not loading with mc-vr-api; it wasn't found!");
        }
        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
