package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.client.vr.VRPluginClientProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api.server.VRServerAPI;

public class VRVerify {

    public static boolean hasAPI;

    static {
        try {
            // Get instances and methods that we expect to be there, so a classnotfound or similar sets having the API to false
            VRAPI.instance();
            VRClientAPI.instance();
            VRServerAPI.instance();
            VRPoseHistory.class.getMethod("netMovement", VRBodyPart.class, int.class, boolean.class);
            hasAPI = true;
        } catch (Throwable ignored) {
            hasAPI = false;
        }
    }

    // Only checks for API if not in-world
    public static boolean clientInVR() {
        return hasAPI && VRPluginClientProxy.vrAPIIInVR();
    }

    public static boolean playerInVR(Player player) {
        return player instanceof ServerPlayer sp ? (hasAPI && VRProxy.vrAPIIInVR(sp)) : clientInVR();
    }
}
