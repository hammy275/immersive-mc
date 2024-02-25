package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.NullStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChiseledBookshelfHandler implements ImmersiveHandler {
    // Used for ChiseledBookShelfBlock to mixin for our redirection
    public static int bookshelfBlockSlotOverride = -1;
    public static InteractionHand bookshelfBlockHandOverride = null;

    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (player.level().getBlockState(pos).getBlock() instanceof ChiseledBookShelfBlock block) {
            bookshelfBlockSlotOverride = slot;
            bookshelfBlockHandOverride = hand;

            block.use(player.level().getBlockState(pos), player.level(), pos, player, hand, null);

            bookshelfBlockSlotOverride = -1;
            bookshelfBlockHandOverride = null;
        }
    }

    @Override
    public boolean usesWorldStorage() {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
        return state.getBlock() == Blocks.CHISELED_BOOKSHELF;
    }

    @Override
    public boolean enabledInServerConfig() {
        return ActiveConfig.FILE.useChiseledBookshelfImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "chiseled_bookshelf");
    }
}
