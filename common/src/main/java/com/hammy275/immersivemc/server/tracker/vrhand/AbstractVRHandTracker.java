package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

public abstract class AbstractVRHandTracker {

    public AbstractVRHandTracker() {
        ServerTrackerInit.vrPlayerTrackers.add(this);
    }

    protected abstract boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand,
                                                VRPose currentVRPose);

    protected abstract void runForHand(Player player, InteractionHand hand, ItemStack stackInHand,
                                       VRPose currentVRData);

    public abstract boolean isEnabledInConfig(ActiveConfig config);

    public void preTick(Player player) {

    }

    public void tick(Player player, VRPose currentVRPose) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (shouldRunForHand(player, hand, player.getItemInHand(hand), currentVRPose)) {
                runForHand(player, hand, player.getItemInHand(hand), currentVRPose);
            }
        }
    }

    protected BlockPos getBlockPosAtHand(VRPose vrPose, InteractionHand hand) {
        VRBodyPartData data = vrPose.getHand(hand);
        return BlockPos.containing(data.getPos());
    }

    protected BlockState getBlockStateAtHand(Player player, VRPose vrPose, InteractionHand hand) {
        return player.level().getBlockState(getBlockPosAtHand(vrPose, hand));
    }

    protected Block getBlockAtHand(Player player, VRPose vrPose, InteractionHand hand) {
        return getBlockStateAtHand(player, vrPose, hand).getBlock();
    }

    protected boolean movingInDirectionWithThreshold(Direction direction, Vec3 handVelocity, double threshold) {
        Vec3i blockFacing = direction.getUnitVec3i();
        // Check velocity requirement with hand velocity for x, y, and z. If the signs match, absolute value them
        // and check that we're moving faster than the threshold. If we are, return true.
        // If we fail for all three, return false.

        // Only one of these paths is travelled, since a Direction only has one non-zero value.
        if (signsMatch(blockFacing.getX(), handVelocity.x) && blockFacing.getX() != 0) {
            return Math.abs(handVelocity.x) >= threshold;
        } else if (signsMatch(blockFacing.getY(), handVelocity.y) && blockFacing.getY() != 0) {
            return Math.abs(handVelocity.y) >= threshold;
        } else if (signsMatch(blockFacing.getZ(), handVelocity.z) && blockFacing.getZ() != 0) {
            return Math.abs(handVelocity.z) >= threshold;
        }
        return false;
    }

    protected boolean signsMatch(double a, double b) {
        return (a < 0d && b < 0d) || (a >= 0d && b >= 0d);
    }
}
