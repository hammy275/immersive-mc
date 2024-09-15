package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ChiseledBookshelfHandler extends ContainerHandler<NullStorage> {
    // Used for ChiseledBookShelfBlock to mixin for our redirection
    public static int bookshelfBlockSlotOverride = -1;
    public static InteractionHand bookshelfBlockHandOverride = null;

    @Override
    public NullStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public NullStorage getEmptyNetworkStorage() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        BlockState state = player.level().getBlockState(pos);
        if (state.getBlock() instanceof ChiseledBookShelfBlock) {
            bookshelfBlockSlotOverride = slot;
            bookshelfBlockHandOverride = hand;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.isEmpty()) {
                state.useWithoutItem(player.level(), player, null);
            } else {
                state.useItemOn(stack, player.level(), player, hand, null);
            }

            bookshelfBlockSlotOverride = -1;
            bookshelfBlockHandOverride = null;
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() == Blocks.CHISELED_BOOKSHELF;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useChiseledBookshelfImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "chiseled_bookshelf");
    }
}
