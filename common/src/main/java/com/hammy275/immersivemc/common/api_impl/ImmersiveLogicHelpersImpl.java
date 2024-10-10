package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.api.server.SwapResult;
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
        return Swap.swapItems(stackFromPlayer, stackInImmersive, swapAmount, null, null);
    }
}
