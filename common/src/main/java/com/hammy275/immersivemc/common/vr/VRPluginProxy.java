package com.hammy275.immersivemc.common.vr;

import net.minecraft.server.level.ServerPlayer;

public class VRPluginProxy {

    public static boolean vrAPIIInVR(ServerPlayer player) {
        return player == null || VRPlugin.API.playerInVR(player);
    }
}
