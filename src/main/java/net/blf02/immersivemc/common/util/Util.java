package net.blf02.immersivemc.common.util;

import net.minecraft.block.ChestBlock;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;

public class Util {

    public static boolean rayTrace(AxisAlignedBB target, Vector3d rayStart, Vector3d rayEnd) {
        // If the start or end of the ray is in the target hitbox, we immediately return true
        if (target.contains(rayStart) || target.contains(rayEnd)) {
            return true;
        }
        // Gets the "hit" for our ray.
        Optional<Vector3d> closestHitOpt = target.clip(rayStart, rayEnd);
        // Return whether or not we have a hit
        return closestHitOpt.isPresent();
    }

    public static Optional<Integer> rayTraceClosest(Vector3d rayStart, Vector3d rayEnd, AxisAlignedBB... targets) {
        double dist = Double.MAX_VALUE;
        Integer winner = null;
        int i = 0;
        for (AxisAlignedBB target : targets) {
            // This is needed since, with chest immersives for example, we don't know
            // if we have a single chest or double chest. As a result, we can have null targets.
            if (target == null) continue;
            // If the start or end of the ray is in the target hitbox, we immediately return true
            if (target.contains(rayStart)) {
                return Optional.of(i);
            }
            // Gets the "hit" for our ray.
            Optional<Vector3d> closestHitOpt = target.clip(rayStart, rayEnd);
            double distTemp = closestHitOpt.isPresent() ? closestHitOpt.get().distanceTo(rayStart) : -1;
            if (closestHitOpt.isPresent() && distTemp < dist) {
                winner = i;
                dist = distTemp;
            }
            i++;
        }
        return Optional.ofNullable(winner);
    }

    public static Optional<Integer> getFirstIntersect(Vector3d pos, AxisAlignedBB... targets) {
        int i = 0;
        for (AxisAlignedBB target : targets) {
            if (target.contains(pos)) {
                return Optional.of(i);
            }
            i++;
        }
        return Optional.empty();
    }

    public static ChestTileEntity getOtherChest(ChestTileEntity chest) {
        // Gets the chest this one is connected to. Can be null.
        Direction otherDir = ChestBlock.getConnectedDirection(chest.getBlockState());
        BlockPos otherPos = chest.getBlockPos().relative(otherDir);
        if (chest.getLevel() != null && chest.getLevel().getBlockEntity(otherPos) instanceof ChestTileEntity) {
            return (ChestTileEntity) chest.getLevel().getBlockEntity(otherPos);
        }
        return null;
    }
}
