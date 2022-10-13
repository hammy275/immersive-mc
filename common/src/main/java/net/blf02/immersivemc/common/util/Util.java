package net.blf02.immersivemc.common.util;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Util {

    public static void addStackToInventory(Player player, ItemStack item) {
        if (!item.isEmpty()) {
            player.getInventory().add(item);
        }
    }

    public static boolean canPickUpItem(ItemEntity item, Player player) {
        /* It seems pickup delay isn't synced client side.
           Although this doesn't cover a lot of use cases, odds are, if an item isn't moving, it can be picked up
           Plus, it somewhat makes sense */
        return (!item.hasPickUpDelay() || player.getAbilities().instabuild)
                && Math.abs(item.getDeltaMovement().x) <= 0.01 && Math.abs(item.getDeltaMovement().z) <= 0.01;
    }

    public static Optional<Integer> rayTraceClosest(Vec3 rayStart, Vec3 rayEnd, AABB... targets) {
        double dist = Double.MAX_VALUE;
        Integer winner = null;
        int i = 0;
        for (AABB target : targets) {
            // This is needed since, with chest immersives for example, we don't know
            // if we have a single chest or double chest. As a result, we can have null targets.
            if (target != null) {
                // If the start or end of the ray is in the target hitbox, we immediately return true
                if (target.contains(rayStart)) {
                    return Optional.of(i);
                }
                // Gets the "hit" for our ray.
                Optional<Vec3> closestHitOpt = target.clip(rayStart, rayEnd);
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

    public static Optional<Integer> getFirstIntersect(Vec3 pos, AABB... targets) {
        int i = 0;
        for (AABB target : targets) {
            if (target != null && target.contains(pos)) {
                return Optional.of(i);
            }
            i++;
        }
        return Optional.empty();
    }

    public static Optional<Integer> getClosestIntersect(Vec3 pos, AABB[] targets, Vec3[] positions) {
        if (targets.length != positions.length) throw new IllegalArgumentException("Targets and positions must be same length!");
        int res = -1;
        double distanceToBeat = Double.MAX_VALUE;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i] != null && targets[i].contains(pos)) {
                double newDist = pos.distanceToSqr(positions[i]);
                if (newDist < distanceToBeat) {
                    distanceToBeat = newDist;
                    res = i;
                }
            }
        }
        return res == -1 ? Optional.empty() : Optional.of(res);
    }

    public static ChestBlockEntity getOtherChest(ChestBlockEntity chest) {
        return getOtherChest(chest, true);
    }

    protected static ChestBlockEntity getOtherChest(ChestBlockEntity chest, boolean checkOther) {
        // Gets the chest this one is connected to. Can be null.
        Direction otherDir = ChestBlock.getConnectedDirection(chest.getBlockState());
        BlockPos otherPos = chest.getBlockPos().relative(otherDir);
        if (chest.getLevel() != null && chest.getLevel().getBlockEntity(otherPos) instanceof ChestBlockEntity) {
            ChestBlockEntity other = (ChestBlockEntity) chest.getLevel().getBlockEntity(otherPos);
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

    public static void setRepeater(Level level, BlockPos pos, int newDelay) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RepeaterBlock) {
            state = state.setValue(RepeaterBlock.DELAY, newDelay);
            level.setBlock(pos, state, 3);
        }
    }

    public static Vec3 getPlayerVelocity(Vec3 lastTickPos, Vec3 currentTickPos) {
        return new Vec3(currentTickPos.x - lastTickPos.x, currentTickPos.y - lastTickPos.y,
                currentTickPos.z - lastTickPos.z);
    }

    public static double moveTowardsZero(double num, double subtract) {
        subtract = Math.abs(subtract);
        if (subtract >= Math.abs(num)) {
            return 0;
        } else if (num < 0) {
            return num + subtract;
        }
        return num - subtract;
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
