package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.server.data.LastTickData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.api.data.VRPose;

/**
 * Hacked extends of AbstractVRHandTracker that's intended to be used for both hands instead of one
 */
public abstract class AbstractVRHandsTracker extends AbstractVRHandTracker {
    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRPose, LastTickData lastVRData) {
        return false; // NO-OP
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRData, LastTickData lastVRData) {
        // NO-OP
    }

    protected abstract boolean shouldRun(Player player, VRPose vrPose, LastTickData lastVRData);

    protected abstract void run(Player player, VRPose vrPose, LastTickData lastVRData);

    @Override
    public void tick(Player player, VRPose currentVRPose, LastTickData lastVRData) {
        if (shouldRun(player, currentVRPose, lastVRData)) {
            run(player, currentVRPose, lastVRData);
        }
    }
}
