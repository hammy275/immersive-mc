package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;

public class ChiseledBookshelfHandler extends ContainerHandler {
    // Used for ChiseledBookShelfBlock to mixin for our redirection
    public static int bookshelfBlockSlotOverride = -1;
    public static InteractionHand bookshelfBlockHandOverride = null;

    @Override
    public NetworkStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public NetworkStorage getEmptyNetworkStorage() {
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
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() == Blocks.CHISELED_BOOKSHELF;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useChiseledBookshelfImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "chiseled_bookshelf");
    }
}
