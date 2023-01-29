package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.client.LastClientVRData;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.world.entity.player.Player;

public class LastVRDataTracker extends AbstractTracker {
    public LastVRDataTracker() {
        // Not added to the client trackers since this is always run at the very end of our tick
    }

    @Override
    protected void tick(Player player) {
        LastClientVRData.addLastTick(VRPlugin.API.getVRPlayer(player));
    }

    @Override
    protected boolean shouldTick(Player player) {
        return VRPluginVerify.clientInVR;
    }
}
