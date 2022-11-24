package com.hammy275.immersivemc_quilt;

import net.blf02.vrapi.api.IVRAPI;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;

public class VRPlugin {
    public static void initVR() {
        List<EntrypointContainer<IVRAPI>> apis = QuiltLoader.getEntrypointContainers("vrapi",
                IVRAPI.class);
        if (apis.size() > 0) {
            com.hammy275.immersivemc.common.vr.VRPlugin.getVRAPI(apis.get(0).getEntrypoint());
        }
    }

}
