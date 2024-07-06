package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.SwapResult;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.api_impl.SwapResultImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ImmersiveLogicHelpersImpl implements ImmersiveLogicHelpers {

    public static final ImmersiveLogicHelpers INSTANCE = new ImmersiveLogicHelpersImpl();

    @Override
    public Direction getHorizontalBlockForward(Player player, BlockPos blockPos) {
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        Vec3 playerPos = player.position();
        Vec3 diff = playerPos.subtract(pos);
        Direction.Axis axis = Math.abs(diff.x) > Math.abs(diff.z) ? Direction.Axis.X : Direction.Axis.Z;
        if (axis == Direction.Axis.X) {
            return diff.x < 0 ? Direction.WEST : Direction.EAST;
        } else {
            return diff.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    @Override
    public SwapResult swapItems(ItemStack stackFromPlayer, ItemStack stackInImmersive, ItemSwapAmount swapAmount) {
        int toPlace = swapAmount.getNumItemsToSwap(stackFromPlayer.getCount());

        // Swap toPlace from stackFromPlayer to otherIn
        ItemStack toHand;
        ItemStack toOther;
        ItemStack leftovers;
        if (Util.stacksEqualBesidesCount(stackFromPlayer, stackInImmersive) && !stackFromPlayer.isEmpty() && !stackInImmersive.isEmpty()) {
            ItemStack stackFromPlayerCountAdjusted = stackFromPlayer.copy();
            stackFromPlayerCountAdjusted.setCount(toPlace);
            Util.ItemStackMergeResult mergeResult = Util.mergeStacks(stackInImmersive.copy(), stackFromPlayerCountAdjusted, false);
            toOther = mergeResult.mergedInto;
            // Take our original hand, shrink by all of the amount to be moved, then grow by the amount
            // that didn't get moved
            toHand = stackFromPlayer.copy();
            toHand.shrink(toPlace);
            toHand.grow(mergeResult.mergedFrom.getCount());
            leftovers = ItemStack.EMPTY;
        } else if (stackFromPlayer.isEmpty()) { // We grab the items from the immersive into our hand
            return new SwapResultImpl(stackInImmersive.copy(), ItemStack.EMPTY, ItemStack.EMPTY);
        } else { // We're placing into a slot of air OR the other slot contains something that isn't what we have
            toOther = stackFromPlayer.copy();
            toOther.setCount(toPlace);
            toHand = stackFromPlayer.copy();
            toHand.shrink(toPlace);
            leftovers = stackInImmersive.copy();
        }
        return new SwapResultImpl(toHand, toOther, leftovers);
    }
}
