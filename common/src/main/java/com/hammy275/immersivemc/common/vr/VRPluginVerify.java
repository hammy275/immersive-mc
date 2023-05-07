package com.hammy275.immersivemc.common.vr;

public class VRPluginVerify {

    public static boolean hasAPI = false;

    // Only checks for API if not in-world
    public static boolean clientInVR() {
        return hasAPI && VRPluginVerifyProxy.vrAPIIInVR();
    }
}
