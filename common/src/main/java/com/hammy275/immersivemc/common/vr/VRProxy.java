package com.hammy275.immersivemc.common.vr;

import net.minecraft.server.level.ServerPlayer;

public class VRProxy {

    public static boolean vrAPIIInVR(ServerPlayer player) {
        // Must get pose since it's possible that the server knows of VR status but hasn't gotten a pose yet
        return player == null || VR.API.getVRPose(player) != null;
    }
}
