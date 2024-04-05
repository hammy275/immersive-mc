package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
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

public class FurnaceHandler extends ContainerHandler {
    @Override
    public NetworkStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return HandlerUtil.makeInventoryContentsFromContainer(player, (Container) player.level.getBlockEntity(pos), 3);
    }

    @Override
    public NetworkStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        WorldlyContainer furnace = (AbstractFurnaceBlockEntity) player.level.getBlockEntity(pos);
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
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useFurnaceImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "furnace");
    }
}
