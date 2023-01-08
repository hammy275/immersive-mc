package com.hammy275.immersivemc_forge;

import net.blf02.vrapi.api.IVRAPI;
import net.blf02.vrapi.api.VRAPIPlugin;
import net.blf02.vrapi.api.VRAPIPluginProvider;

@VRAPIPlugin
public class VRPlugin implements VRAPIPluginProvider {

    public static IVRAPI API;

    @Override
    public void getVRAPI(IVRAPI ivrapi) {
        com.hammy275.immersivemc.common.vr.VRPlugin.getVRAPI(ivrapi);
    }
}