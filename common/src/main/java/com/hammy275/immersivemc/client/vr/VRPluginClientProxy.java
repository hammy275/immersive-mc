package com.hammy275.immersivemc.client.vr;

import net.minecraft.client.Minecraft;
import org.vivecraft.api.client.VRClientAPI;

public class VRPluginClientProxy {

    public static boolean vrAPIIInVR() {
        return Minecraft.getInstance().player == null ||
                VRClientAPI.instance().getPreTickWorldPose() != null;
    }

}
