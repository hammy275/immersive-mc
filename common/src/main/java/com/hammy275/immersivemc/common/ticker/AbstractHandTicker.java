package com.hammy275.immersivemc.common.ticker;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

public abstract class AbstractHandTicker extends AbstractTicker {
    @Override
    protected final void tick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        for (InteractionHand hand : InteractionHand.values()) {
            VRBodyPartData handData = pose.getHand(hand);
            if (shouldTickHand(player, hand, handData, poseHistory)) {
                tickHand(player, hand, handData, poseHistory);
            }
        }
    }

    @Override
    protected final boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        return true;
    }

    protected abstract void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory);

    protected abstract boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory);
}
