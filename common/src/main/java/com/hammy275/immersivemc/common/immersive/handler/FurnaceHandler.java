package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceHandler implements ImmersiveHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return HandlerUtil.makeInventoryContentsFromContainer(player, (Container) player.level().getBlockEntity(pos), 3);
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        WorldlyContainer furnace = (AbstractFurnaceBlockEntity) player.level().getBlockEntity(pos);
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            if (slot != 1 || furnace.canPlaceItem(1, playerItem) || playerItem.isEmpty()) {
                Swap.SwapResult result = Swap.getSwap(playerItem, furnaceItem, mode);
                Swap.givePlayerItemSwap(result.toHand, playerItem, player, hand);
                furnace.setItem(slot, result.toOther);
                Util.placeLeftovers(player, result.leftovers);
            }
        } else {
            boolean itemTaken = false;
            if (playerItem.isEmpty()) {
                player.setItemInHand(hand, furnaceItem);
                furnace.setItem(2, playerItem);
                itemTaken = true;
            } else if (Util.stacksEqualBesidesCount(furnaceItem, playerItem)) {
                int beforeGrabCount = furnace.getItem(2).getCount();
                Util.ItemStackMergeResult result = Util.mergeStacks(playerItem, furnaceItem, false);
                player.setItemInHand(hand, result.mergedInto);
                furnace.setItem(slot, result.mergedFrom);
                itemTaken = furnace.getItem(2).isEmpty() || furnace.getItem(2).getCount() < beforeGrabCount;
            }

            // Experience and recipes reward. Given only if at least one item was taken. This is the same as
            // vanilla behavior.
            if (itemTaken && furnace instanceof AbstractFurnaceBlockEntity afbe) {
                afbe.awardUsedRecipesAndPopExperience(player);
            }
        }
        furnace.setChanged();
    }

    @Override
    public boolean usesWorldStorage() {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
        return blockEntity instanceof AbstractFurnaceBlockEntity;
    }

    @Override
    public boolean enabledInServerConfig() {
        return ActiveConfig.FILE.useFurnaceImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "furnace");
    }
}
