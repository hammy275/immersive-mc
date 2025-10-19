package com.hammy275.immersivemc.server.tracker.vrhand;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.api.data.VRPose;

/**
 * Hacked extends of AbstractVRHandTracker that's intended to be used for both hands instead of one
 */
public abstract class AbstractVRHandsTracker extends AbstractVRHandTracker {
    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRPose) {
        return false; // NO-OP
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRData) {
        // NO-OP
    }

    protected abstract boolean shouldRun(Player player, VRPose vrPose);

    protected abstract void run(Player player, VRPose vrPose);

    @Override
    public void tick(Player player, VRPose currentVRPose) {
        if (shouldRun(player, currentVRPose)) {
            run(player, currentVRPose);
        }
    }
}
