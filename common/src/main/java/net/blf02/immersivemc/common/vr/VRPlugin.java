package net.blf02.immersivemc.common.vr;

import net.blf02.vrapi.api.IVRAPI;

public class VRPlugin {

    public static IVRAPI API;

    public static void getVRAPI(IVRAPI ivrapi) {
        API = ivrapi;
        VRPluginVerify.hasAPI = true;
        // Register Client VR Subscriber if we're running client side and we have VR

    }
}
