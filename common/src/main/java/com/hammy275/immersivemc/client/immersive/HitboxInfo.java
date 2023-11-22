package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class HitboxInfo implements Cloneable {

    // Settings
    public final Function<BuiltImmersiveInfo, Vec3> centerOffset;
    public final double sizeX;
    public final double sizeY;
    public final double sizeZ;
    public final boolean isInput;
    public final boolean holdsItems;
    public final Direction upDownRenderDir;
    public final boolean itemSpins;
    public final float itemRenderSizeMultiplier;
    public final boolean isTriggerHitbox;
    public final Function<BuiltImmersiveInfo, Component> textSupplier;

    // Calculated data
    private AABB box;
    private Vec3 pos;
    boolean didCalc = false; // One-time flag to make sure recalculate() is called before getting data.

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    public HitboxInfo(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ,
                      boolean holdsItems, boolean isInput,
                      Direction upDownRenderDir, boolean itemSpins, float itemRenderSizeMultiplier,
                      boolean isTriggerHitbox, Function<BuiltImmersiveInfo, Component> textSupplier) {
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.holdsItems = holdsItems;
        this.isInput = isInput;
        this.upDownRenderDir = upDownRenderDir;
        this.itemSpins = itemSpins;
        this.itemRenderSizeMultiplier = itemRenderSizeMultiplier;
        this.isTriggerHitbox = isTriggerHitbox;
        this.textSupplier = textSupplier;
    }

    /**
     * Force the position and box of this hitbox to be null.
     */
    public void forceNull() {
        this.pos = null;
        this.box = null;
        this.didCalc = true;
    }

    public void recalculate(Level level, HitboxPositioningMode mode, BuiltImmersiveInfo info) {
        didCalc = true;
        Vec3 offset = this.centerOffset.apply(info);
        if (offset == null) {
            forceNull();
            return; // Bail early if we don't actually have a position to work with
        }
        BlockPos pos = info.getBlockPosition();
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcHorizBlockFacing(blockFacing, info, offset);
        } else if (mode == HitboxPositioningMode.PLAYER_FACING) {
            Direction blockFacing = Minecraft.getInstance().player.getDirection();
            Vec3 xVec = Vec3.atLowerCornerOf(blockFacing.getClockWise().getNormal());
            Vec3 yVec = Vec3.atLowerCornerOf(blockFacing.getNormal());
            Vec3 zVec = new Vec3(0, 1, 0);

            Vec3 centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
            double actualYSize = this.sizeZ;
            double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;

            this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
            this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
        } else if (mode == HitboxPositioningMode.TOP_LITERAL) {
            Vec3 xVec = new Vec3(1, 0, 0);
            Vec3 yVec = new Vec3(0, 1, 0);
            Vec3 zVec = new Vec3(0, 0, 1);

            Vec3 centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            double actualXSize = this.sizeX;
            double actualYSize = this.sizeY;
            double actualZSize = this.sizeZ;

            this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
            this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
        } else if (mode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            recalcTopBottomBlockFacing(blockFacing, info, offset, false);
        } else if (mode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            Direction blockFacing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
            Vec3 xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
            Vec3 yVec = new Vec3(0, 1, 0);
            Vec3 zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

            Vec3 centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                    .add(xVec.scale(0.5)).add(yVec.scale(0.5));

            double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
            double actualYSize = this.sizeY;
            double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;

            this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
            this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
        } else if (mode == HitboxPositioningMode.BLOCK_FACING_NEG_X) {
            // Delegate to other calculation modes
            Direction blockFacing = level.getBlockState(pos).getValue(DirectionalBlock.FACING);
            if (blockFacing.getAxis() != Direction.Axis.Y) {
                recalcHorizBlockFacing(blockFacing, info, offset);
            } else {
                // Pretend the block is facing west so that way west becomes +x
                recalcTopBottomBlockFacing(Direction.WEST, info, offset, blockFacing == Direction.DOWN);
            }
        } else {
            throw new UnsupportedOperationException("Hitbox calculation for positioning mode " + mode + " unimplemented!");
        }
    }

    private void recalcHorizBlockFacing(Direction blockFacing, BuiltImmersiveInfo info, Vec3 offset) {
        BlockPos pos = info.getBlockPosition();

        // Vectors that are combined with centerOffset. May not necessarily correspond to the actual in-game axis.
        // For example, zVec corresponds always to the in-game Y-axis for PLAYER_FACING (such as crafting tables).
        Vec3 xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
        Vec3 yVec = new Vec3(0, 1, 0);
        Vec3 zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

        Vec3 centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                .add(xVec.scale(0.5)).add(yVec.scale(0.5));

        // If, for example, the furnace is facing the X-axis, then the size should come from sizeZ, since the
        // Z size represents going "into"/"out of" the furnace (as the X size is for left and right on the furnace's
        // face, and the y size is for going up or down the face).
        // The actual hitbox size on the in-game x, y, and z axis.
        double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
        double actualYSize = this.sizeY;
        double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    private void recalcTopBottomBlockFacing(Direction blockFacing, BuiltImmersiveInfo info, Vec3 offset, boolean bottomOfBlock) {

        BlockPos pos = info.getBlockPosition();

        Vec3 xVec = Vec3.atLowerCornerOf(blockFacing.getClockWise().getNormal());
        Vec3 yVec = Vec3.atLowerCornerOf(blockFacing.getNormal());
        Vec3 zVec = new Vec3(0, 1, 0);

        Vec3 centerPos = Vec3.atBottomCenterOf(pos).add(0, bottomOfBlock ? 0 : 1, 0);

        double actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
        double actualYSize = this.sizeZ;
        double actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;

        this.pos = centerPos.add(xVec.scale(offset.x)).add(yVec.scale(offset.y)).add(zVec.scale(offset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    public AABB getAABB() {
        if (!didCalc) {
            throw new IllegalStateException("Should call recalculate() or forceNull() before getting hitbox.");
        }
        return box;
    }

    public Vec3 getPos() {
        if (!didCalc) {
            throw new IllegalStateException("Should call recalculate() or forceNull() before getting position.");
        }
        return pos;
    }

    public boolean hasPos() {
        return pos != null;
    }

    public boolean hasAABB() {
        return box != null;
    }

    public boolean calcDone() {
        return didCalc;
    }

    @Override
    public Object clone() {
        return cloneWithNewOffset(centerOffset);
    }

    /**
     * Clones this HitboxInfo replacing the offset with the supplied one.
     * @param newOffset New offset for the clone.
     * @return Clone with the offset replaced with newOffset.
     */
    public HitboxInfo cloneWithNewOffset(Function<BuiltImmersiveInfo, Vec3> newOffset) {
        return new HitboxInfo(newOffset, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }

    /**
     * Clones this HitboxInfo, offsetting the position by the supplied offset.
     * @param offset Offset relative to this HitboxInfo's offset.
     * @return HitboxInfo that is offset by offset in comparison to this HitboxInfo.
     */
    public HitboxInfo cloneWithAddedOffset(Vec3 offset) {
        return new HitboxInfo((info) -> {
            Vec3 offsetOut = centerOffset.apply(info);
            if (offsetOut == null) {
                return null;
            }
            return offsetOut.add(offset);
        }, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }
}
