package com.hammy275.immersivemc.common.vr.dev.impl;

import com.hammy275.immersivemc.common.vr.dev.DevVRState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

public class DevVRAPI implements VRAPI {
    @Override
    public boolean isVRPlayer(Player player) {
        return DevVRState.inVR;
    }

    @Override
    public VRPose getVRPose(Player player) {
        // This dev feature is only intended to be used in singleplayer or by LAN hosts.
        // Watch as it reaches directly across the client<-->server boundary!
        if (Minecraft.getInstance().player != null && player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            return DevVRState.pose;
        } else {
            return VRAPI.instance().getVRPose(player);
        }
    }

    @Override
    public VRPoseHistory getHistoricalVRPoses(Player player) {
        // This dev feature is only intended to be used in singleplayer or by LAN hosts.
        // Watch as it reaches directly across the client<-->server boundary!
        if (Minecraft.getInstance().player != null && player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            return DevVRState.poseHistory;
        } else {
            return VRAPI.instance().getHistoricalVRPoses(player);
        }
    }
}
