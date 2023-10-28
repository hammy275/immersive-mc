package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class HitboxInfo implements Cloneable {

    // Settings
    public final Vec3 centerOffset;
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

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    public HitboxInfo(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ,
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

    public void recalculate(Level level, BlockPos pos, HitboxPositioningMode mode) {
        // Vectors that are combined with centerOffset. May not necessarily correspond to the actual in-game axis.
        // For example, zVec corresponds always to the in-game Y-axis for PLAYER_FACING (such as crafting tables).
        Vec3 xVec;
        Vec3 yVec;
        Vec3 zVec;

        // The actual hitbox size on the in-game x, y, and z axis.
        double actualXSize;
        double actualYSize;
        double actualZSize;
        Vec3 centerPos;
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
            yVec = new Vec3(0, 1, 0);
            zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

            centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                    .add(xVec.scale(0.5)).add(yVec.scale(0.5));

            // If, for example, the furnace is facing the X-axis, then the size should come from sizeZ, since the
            // Z size represents going "into"/"out of" the furnace (as the X size is for left and right on the furnace's
            // face, and the y size is for going up or down the face).
            actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
            actualYSize = this.sizeY;
            actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
        } else if (mode == HitboxPositioningMode.PLAYER_FACING) {
            Direction playerFacing = Minecraft.getInstance().player.getDirection();
            xVec = Vec3.atLowerCornerOf(playerFacing.getClockWise().getNormal());
            yVec = Vec3.atLowerCornerOf(playerFacing.getNormal());
            zVec = new Vec3(0, 1, 0);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            actualXSize = playerFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
            actualYSize = this.sizeZ;
            actualZSize = playerFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;
        } else if (mode == HitboxPositioningMode.TOP_LITERAL) {
            xVec = new Vec3(1, 0, 0);
            yVec = new Vec3(0, 1, 0);
            zVec = new Vec3(0, 0, 1);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            actualXSize = this.sizeX;
            actualYSize = this.sizeY;
            actualZSize = this.sizeZ;
        } else if (mode == HitboxPositioningMode.TOP_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            xVec = Vec3.atLowerCornerOf(blockFacing.getClockWise().getNormal());
            yVec = Vec3.atLowerCornerOf(blockFacing.getNormal());
            zVec = new Vec3(0, 1, 0);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);

            actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeY : sizeX;
            actualYSize = this.sizeZ;
            actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeX : sizeY;
        } else if (mode == HitboxPositioningMode.HORIZONTAL_PLAYER_FACING) {
            Direction blockFacing = AbstractImmersive.getForwardFromPlayer(Minecraft.getInstance().player);
            xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
            yVec = new Vec3(0, 1, 0);
            zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

            centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                    .add(xVec.scale(0.5)).add(yVec.scale(0.5));

            actualXSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
            actualYSize = this.sizeY;
            actualZSize = blockFacing.getAxis() == Direction.Axis.X ? sizeZ : sizeX;
        } else {
            throw new UnsupportedOperationException("Hitbox calculation for positioning mode " + mode + " unimplemented!");
        }
        this.pos = centerPos.add(xVec.scale(centerOffset.x)).add(yVec.scale(centerOffset.y)).add(zVec.scale(centerOffset.z));
        this.box = AABB.ofSize(this.pos, actualXSize, actualYSize, actualZSize);
    }

    public AABB getAABB() {
        if (box == null) {
            throw new IllegalStateException("Should call recalculate() before getting hitbox.");
        }
        return box;
    }

    public Vec3 getPos() {
        if (pos == null) {
            throw new IllegalStateException("Should call recalculate() before getting position.");
        }
        return pos;
    }

    public boolean hasPos() {
        return pos != null;
    }

    public boolean hasAABB() {
        return box != null;
    }

    @Override
    public Object clone() {
        return cloneWithOffset(centerOffset);
    }

    public HitboxInfo cloneWithOffset(Vec3 newOffset) {
        return new HitboxInfo(newOffset, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }
}
