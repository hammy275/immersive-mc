package com.hammy275.immersivemc.server.swap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;

public class ChiseledBookshelfSwap {

    // Used for ChiseledBookShelfBlock to mixin for our redirection
    public static int bookshelfBlockSlotOverride = -1;
    public static InteractionHand bookshelfBlockHandOverride = null;

    public static void swap(ServerPlayer player, BlockPos pos, int slot, InteractionHand hand) {
        if (player.level().getBlockState(pos).getBlock() instanceof ChiseledBookShelfBlock block) {
            bookshelfBlockSlotOverride = slot;
            bookshelfBlockHandOverride = hand;

            block.use(player.level().getBlockState(pos), player.level(), pos, player, hand, null);

            bookshelfBlockSlotOverride = -1;
            bookshelfBlockHandOverride = null;
        }

    }
}
