package com.hammy275.immersivemc.neoforge;

import net.blf02.neoforge.VRAPIPlugin;
import net.blf02.neoforge.VRAPIPluginProvider;
import net.blf02.vrapi.api.IVRAPI;

@VRAPIPlugin
public class VRPlugin implements VRAPIPluginProvider {

    public static IVRAPI API;

    @Override
    public void getVRAPI(IVRAPI ivrapi) {
        com.hammy275.immersivemc.common.vr.VRPlugin.getVRAPI(ivrapi);
    }
}