package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.client.vr.VRPluginClientProxy;
import net.minecraft.server.level.ServerPlayer;

public class VRPluginVerify {

    public static boolean hasAPI = false;

    // Only checks for API if not in-world
    public static boolean clientInVR() {
        return hasAPI && VRPluginClientProxy.vrAPIIInVR();
    }

    public static boolean playerInVR(ServerPlayer player) {
        return hasAPI && VRPluginProxy.vrAPIIInVR(player);
    }
}
