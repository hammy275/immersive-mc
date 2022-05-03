package net.blf02.immersivemc.common.util;

import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
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
            if (target != null) {
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
            }
            i++;
        }
        return Optional.ofNullable(winner);
    }

    public static Optional<Integer> getFirstIntersect(Vector3d pos, AxisAlignedBB... targets) {
        int i = 0;
        for (AxisAlignedBB target : targets) {
            if (target != null && target.contains(pos)) {
                return Optional.of(i);
            }
            i++;
        }
        return Optional.empty();
    }

    public static ChestTileEntity getOtherChest(ChestTileEntity chest) {
        return getOtherChest(chest, true);
    }

    protected static ChestTileEntity getOtherChest(ChestTileEntity chest, boolean checkOther) {
        // Gets the chest this one is connected to. Can be null.
        Direction otherDir = ChestBlock.getConnectedDirection(chest.getBlockState());
        BlockPos otherPos = chest.getBlockPos().relative(otherDir);
        if (chest.getLevel() != null && chest.getLevel().getBlockEntity(otherPos) instanceof ChestTileEntity) {
            ChestTileEntity other = (ChestTileEntity) chest.getLevel().getBlockEntity(otherPos);
            if (checkOther && other != null) { // Make sure the chest we think we're connected to is connected back to us
                return getOtherChest(other, false) == chest ? other : null;
            }
            return other;
        }
        return null;
    }

    public static boolean stacksEqualBesidesCount(ItemStack a, ItemStack b) {
        int oldCountA = a.getCount();
        int oldCountB = b.getCount();
        a.setCount(1);
        b.setCount(1);
        boolean res = ItemStack.matches(a, b);
        a.setCount(oldCountA);
        b.setCount(oldCountB);
        return res;
    }

    /**
     * Merges two ItemStacks together
     * @param mergeIntoIn ItemStack to merge into
     * @param mergeFromIn ItemStack to merge from
     * @param useCopy Whether or not to use copies of the ItemStacks supplied
     * @return An ItemStackMergeResult containing the results post-merge.
     * If no merge takes place, the returned result just contains the inputted ItemStacks.
     */
    public static ItemStackMergeResult mergeStacks(ItemStack mergeIntoIn, ItemStack mergeFromIn, boolean useCopy) {
        if (!stacksEqualBesidesCount(mergeIntoIn, mergeFromIn) || mergeIntoIn.getMaxStackSize() <= 1) {
            return new ItemStackMergeResult(mergeIntoIn, mergeFromIn);
        }
        ItemStack into = useCopy ? mergeIntoIn.copy() : mergeIntoIn;
        ItemStack from = useCopy ? mergeFromIn.copy() : mergeFromIn;
        int totalCount = into.getCount() + from.getCount();
        int fromAmount = 0;
        if (totalCount > into.getMaxStackSize()) {
            fromAmount = totalCount - into.getMaxStackSize();
            totalCount = into.getMaxStackSize();
        }
        into.setCount(totalCount);
        from.setCount(fromAmount);
        return new ItemStackMergeResult(into, fromAmount == 0 ? ItemStack.EMPTY : from);
    }

    public static class ItemStackMergeResult {

        public final ItemStack mergedInto;
        public final ItemStack mergedFrom;

        public ItemStackMergeResult(ItemStack mergedInto, ItemStack mergedFrom) {
            this.mergedInto = mergedInto;
            this.mergedFrom = mergedFrom;
        }

        @Override
        public String toString() {
            return "Merged Into: " + this.mergedInto + "\n" + "Merged From: " + this.mergedFrom;
        }
    }
}
