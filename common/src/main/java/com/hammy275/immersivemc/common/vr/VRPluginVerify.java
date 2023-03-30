package com.hammy275.immersivemc.common.vr;

public class VRPluginVerify {

    public static boolean hasAPI = false;
    public static boolean clientInVR() {
        return hasAPI && VRPluginVerifyProxy.vrAPIIInVR();
    }
}
