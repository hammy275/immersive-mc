package com.hammy275.immersivemc.common.vr;

import net.minecraft.client.Minecraft;

public class VRPluginVerifyProxy {

    public static boolean vrAPIIInVR() {
        return Minecraft.getInstance().player == null ||
                VRPlugin.API.playerInVR(Minecraft.getInstance().player);
    }
}
