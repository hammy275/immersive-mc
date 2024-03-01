package com.hammy275.immersivemc.client.vr;

import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.minecraft.client.Minecraft;

public class VRPluginClientProxy {

    public static boolean vrAPIIInVR() {
        return Minecraft.getInstance().player == null ||
                VRPlugin.API.playerInVR(Minecraft.getInstance().player);
    }

}
