package com.hammy275.immersivemc.common.vr;

import net.minecraft.client.Minecraft;

public class VRPluginProxy {

    public static boolean vrAPIIInVR() {
        return Minecraft.getInstance().player == null ||
                VRPlugin.API.playerInVR(Minecraft.getInstance().player);
    }

}
