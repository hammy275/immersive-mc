package com.hammy275.immersivemc.client.immersive;

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

    // Calculated data
    private AABB box;
    private Vec3 pos;

    // Extra data. Note that things should only be stored here after a clone() call.
    public ItemStack item = null;

    public HitboxInfo(Vec3 centerOffset, double size, boolean isInput, boolean holdsItems) {
        this.centerOffset = centerOffset;
        this.size = size;
        this.isInput = isInput;
        this.holdsItems = holdsItems;
    }

    public void recalculate(Level level, BlockPos pos, HitboxPositioningMode mode) {
        if (mode == HitboxPositioningMode.HORIZONTAL_BLOCK_FACING) {
            Direction blockFacing = level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            Vec3 xVec = Vec3.atLowerCornerOf(blockFacing.getCounterClockWise().getNormal());
            Vec3 yVec = new Vec3(0, 1, 0);
            Vec3 zVec = Vec3.atLowerCornerOf(blockFacing.getOpposite().getNormal());

            Vec3 centerPos = AbstractImmersive.getDirectlyInFront(blockFacing, pos)
                    .add(xVec.scale(0.5)).add(yVec.scale(0.5));

            this.pos = centerPos.add(xVec.scale(centerOffset.x)).add(yVec.scale(centerOffset.y)).add(zVec.scale(centerOffset.z));
            this.box = AABB.ofSize(this.pos, size, size, size);
        }
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
        try {
            return super.clone();
        } catch (CloneNotSupportedException ignored) {
            // Should be unreachable, as this object implements Cloneable, so can be cloned.
            throw new UnsupportedOperationException("Not cloneable!");
        }

    }
}
