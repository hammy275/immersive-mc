package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.client.tracker.vr.FishingReelTrackerCore;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.world.entity.player.Player;

public class FishingReelTracker extends AbstractTracker {

    public FishingReelTracker() {
        ClientTrackerInit.trackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        FishingReelTrackerCore.tick(player);
    }

    @Override
    protected boolean shouldTick(Player player) {
        return VRPluginVerify.clientInVR() && FishingReelTrackerCore.shouldTick(player);
    }
}
