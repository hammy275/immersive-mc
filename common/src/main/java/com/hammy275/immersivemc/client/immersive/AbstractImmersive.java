package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.api_impl.ImmersiveRenderHelpersImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Currently just a bunch of utils. Will be cleaned up and removed.
 */
public abstract class AbstractImmersive {
    public static final int maxLight = LightTexture.pack(15, 15);

    public static void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue) {
        renderHitbox(stack, hitbox, alwaysRender, red, green, blue, 1);
    }

    public static void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender,
                                    float red, float green, float blue, float alpha) {
        ImmersiveRenderHelpersImpl.INSTANCE.renderHitbox(stack, hitbox, alwaysRender, red, green, blue, alpha);
    }

    /**
     * Gets the direction to the left from the Direction's perspective, assuming the Direction is
     * looking at the player. This makes it to the right for the player.
     * @param forward Forward direction of the block
     * @return The aforementioned left
     */
    public static Direction getLeftOfDirection(Direction forward) {
        if (forward == Direction.UP || forward == Direction.DOWN) {
            throw new IllegalArgumentException("Direction cannot be up or down!");
        }
        if (forward == Direction.NORTH) {
            return Direction.WEST;
        } else if (forward == Direction.WEST) {
            return Direction.SOUTH;
        } else if (forward == Direction.SOUTH) {
            return Direction.EAST;
        }
        return Direction.NORTH;
    }

    public static Vec3 getTopCenterOfBlock(BlockPos pos) {
        // Only add 0.5 to y since atCenterOf moves it up 0.5 for us
        return Vec3.upFromBottomCenterOf(pos, 1);
    }

    /**
     * Gets the forward direction of the block based on the player
     *
     * Put simply, this returns the best direction such that the block is "facing" the play by looking that direction.
     * @param player Player to get forward from
     * @param pos Blosck position of the
     * @return The forward direction of a block to use.
     */
    public static Direction getForwardFromPlayer(Player player, BlockPos pos) {
        Vec3 blockPos = Vec3.atBottomCenterOf(pos);
        Vec3 playerPos = player.position();
        Vec3 diff = playerPos.subtract(blockPos);
        Direction.Axis axis = Math.abs(diff.x) > Math.abs(diff.z) ? Direction.Axis.X : Direction.Axis.Z;
        if (axis == Direction.Axis.X) {
            return diff.x < 0 ? Direction.WEST : Direction.EAST;
        } else {
            return diff.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    public static Vec3[] get3x3HorizontalGrid(BlockPos blockPos, double spacing, Direction blockForward,
                                       boolean use3DCompat) {
        Vec3 pos = getTopCenterOfBlock(blockPos);
        if (use3DCompat) {
            pos = pos.add(0, 1d/16d, 0);
        }
        Direction left = getLeftOfDirection(blockForward);

        Vec3 leftOffset = new Vec3(
                left.getNormal().getX() * -spacing, 0, left.getNormal().getZ() * -spacing);
        Vec3 rightOffset = new Vec3(
                left.getNormal().getX() * spacing, 0, left.getNormal().getZ() * spacing);

        Vec3 topOffset = new Vec3(
                blockForward.getNormal().getX() * -spacing, 0, blockForward.getNormal().getZ() * -spacing);
        Vec3 botOffset = new Vec3(
                blockForward.getNormal().getX() * spacing, 0, blockForward.getNormal().getZ() * spacing);


        return new Vec3[]{
                pos.add(leftOffset).add(topOffset), pos.add(topOffset), pos.add(rightOffset).add(topOffset),
                pos.add(leftOffset), pos, pos.add(rightOffset),
                pos.add(leftOffset).add(botOffset), pos.add(botOffset), pos.add(rightOffset).add(botOffset)
        };
    }

    public static Direction getForwardFromPlayerUpAndDown(Player player, BlockPos pos) {
        return getForwardFromPlayerUpAndDownFilterBlockFacing(player, pos, false);
    }

    /**
     * Same as getForwardFromPlayer, but can return the block facing up or down, alongside any of the four
     * directions of N/E/S/W.
     * @param player Player.
     * @param pos Position of block.
     * @param filterOnBlockFacing If true, the axis of the block's DirectionalBlock.FACING will not be returned from
     *                            this function. The block should have this property if this is true, of course!
     * @return Any Direction, representing what direction the block should be facing based on the player's position.
     */
    public static Direction getForwardFromPlayerUpAndDownFilterBlockFacing(Player player, BlockPos pos, boolean filterOnBlockFacing) {
        Direction.Axis filter = filterOnBlockFacing ? player.level().getBlockState(pos).getValue(DirectionalBlock.FACING).getAxis() : null;
        Vec3 playerPos = player.position();
        if (playerPos.y >= pos.getY() + 0.625 && filter != Direction.Axis.Y) {
            return Direction.UP;
        } else if (playerPos.y <= pos.getY() - 0.625 && filter != Direction.Axis.Y) {
            return Direction.DOWN;
        } else {
            Direction forward = getForwardFromPlayer(player, pos);
            if (forward.getAxis() != filter) {
                return forward;
            } else {
                // We filter on non-Y axis, and getForwardFromPlayer was on our filter. Find the closest and get it.
                Direction blockFacing = player.level().getBlockState(pos).getValue(DirectionalBlock.FACING);
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                Direction blockLeftDir = blockFacing.getCounterClockWise();
                Vec3 blockLeftVec = new Vec3(blockLeftDir.getNormal().getX(), blockLeftDir.getNormal().getY(), blockLeftDir.getNormal().getZ());
                Vec3 counterClockwisePos = blockCenter.add(blockLeftVec.scale(0.5));
                Vec3 clockwisePos = blockCenter.add(blockLeftVec.scale(-0.5));
                Vec3 upPos = blockCenter.add(0, 0.5, 0);
                Vec3 downPos = blockCenter.add(0, -0.5, 0);

                double counterClockwiseDist = counterClockwisePos.distanceToSqr(playerPos);
                double clockwiseDist = clockwisePos.distanceToSqr(playerPos);
                double upDist = upPos.distanceToSqr(playerPos);
                double downDist = downPos.distanceToSqr(playerPos);

                double min = Math.min(counterClockwiseDist, clockwiseDist);
                min = Math.min(min, upDist);
                min = Math.min(min, downDist);

                if (min == counterClockwiseDist) {
                    return forward.getCounterClockWise();
                } else if (min == clockwiseDist) {
                    return forward.getClockWise();
                } else if (min == upDist) {
                    return Direction.UP;
                } else {
                    return Direction.DOWN;
                }
            }
        }
    }

}
