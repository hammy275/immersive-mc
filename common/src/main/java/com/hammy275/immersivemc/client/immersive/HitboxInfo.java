package com.hammy275.immersivemc.client.immersive;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HitboxInfo implements Cloneable {

    // Settings
    public final Vec3 centerOffset;
    public final double size;
    public final boolean isInput;
    public final boolean holdsItems;
    public final Direction upDownRenderDir;
    public final boolean itemSpins;
    public final float itemRenderSizeMultiplier;

    // Calculated data
    private AABB box;
    private Vec3 pos;

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    public HitboxInfo(Vec3 centerOffset, double size, boolean holdsItems, boolean isInput,
                      Direction upDownRenderDir, boolean itemSpins, float itemRenderSizeMultiplier) {
        this.centerOffset = centerOffset;
        this.size = size;
        this.holdsItems = holdsItems;
        this.isInput = isInput;
        this.upDownRenderDir = upDownRenderDir;
        this.itemSpins = itemSpins;
        this.itemRenderSizeMultiplier = itemRenderSizeMultiplier;
    }

    public void recalculate(Level level, BlockPos pos, HitboxPositioningMode mode) {
        Vec3 xVec;
        Vec3 yVec;
        Vec3 zVec;
        Vec3 centerPos;
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
            yVec = new Vec3(0, 1, 0);
            zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

            centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                    .add(xVec.scale(0.5)).add(yVec.scale(0.5));
        } else if (mode == HitboxPositioningMode.PLAYER_FACING) {
            Direction playerFacing = Minecraft.getInstance().player.getDirection();
            xVec = Vec3.atLowerCornerOf(playerFacing.getClockWise().getNormal());
            yVec = Vec3.atLowerCornerOf(playerFacing.getNormal());
            zVec = new Vec3(0, 1, 0);

            centerPos = Vec3.atBottomCenterOf(pos).add(0, 1, 0);
        } else {
            throw new UnsupportedOperationException("Hitbox calculation for positioning mode " + mode + " unimplemented!");
        }
        this.pos = centerPos.add(xVec.scale(centerOffset.x)).add(yVec.scale(centerOffset.y)).add(zVec.scale(centerOffset.z));
        this.box = AABB.ofSize(this.pos, size, size, size);
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
        return new HitboxInfo(newOffset, size, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier);
    }
}
