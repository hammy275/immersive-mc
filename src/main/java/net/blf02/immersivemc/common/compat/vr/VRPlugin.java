package net.blf02.immersivemc.common.compat.vr;

import net.blf02.vrapi.api.IVRAPI;
import net.blf02.vrapi.api.VRAPIPlugin;
import net.blf02.vrapi.api.VRAPIPluginProvider;

@VRAPIPlugin
public class VRPlugin implements VRAPIPluginProvider {

    public static IVRAPI API;

    @Override
    public void getVRAPI(IVRAPI ivrapi) {
        VRPlugin.API = ivrapi;
        VRStatus.hasVR = true;
    }
}
