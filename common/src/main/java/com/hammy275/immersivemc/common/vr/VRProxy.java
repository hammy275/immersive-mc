package com.hammy275.immersivemc.common.vr;

import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.api.VRAPI;

public class VRProxy {

    public static boolean vrAPIIInVR(ServerPlayer player) {
        return player == null || VRAPI.instance().isVRPlayer(player);
    }
}
