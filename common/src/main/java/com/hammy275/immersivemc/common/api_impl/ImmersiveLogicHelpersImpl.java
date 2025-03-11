package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.api_impl.SwapResultImpl;
import com.hammy275.immersivemc.server.storage.server.ItemSwapAmount;
import com.hammy275.immersivemc.server.storage.server.SwapResult;
import com.hammy275.immersivemc.server.swap.Swap;
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
        return swapItems(stackFromPlayer, stackInImmersive, swapAmount, -1);
    }

    @Override
    public SwapResult swapItems(ItemStack stackFromPlayer, ItemStack stackInImmersive, ItemSwapAmount swapAmount, int forcedMaxImmersiveStackSize) {
        return Swap.swapItems(stackFromPlayer, stackInImmersive, swapAmount, forcedMaxImmersiveStackSize, null, null);
    }

    @Override
    public SwapResult swapItemsWithOutput(ItemStack stackFromPlayer, ItemStack stackInImmersive) {
        if (stackFromPlayer.isEmpty()) { // All items moved directly to player hand
            return new SwapResultImpl(stackInImmersive.copy(), ItemStack.EMPTY, ItemStack.EMPTY);
        } else if (Util.stacksEqualBesidesCount(stackInImmersive, stackFromPlayer) && stackFromPlayer.getCount() < stackFromPlayer.getMaxStackSize()) {
            Util.ItemStackMergeResult result = Util.mergeStacks(stackFromPlayer, stackInImmersive, true);
            return new SwapResultImpl(result.mergedInto, result.mergedFrom, ItemStack.EMPTY);
        } else { // Player hand item can't hold any of immerisve item. Set player item same and spawn leftovers.
            return new SwapResultImpl(stackFromPlayer, ItemStack.EMPTY, stackInImmersive.copy());
        }
    }
}
