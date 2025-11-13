package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapResult;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceHandler extends ContainerHandler<ListOfItemsStorage> {
    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return HandlerUtil.makeInventoryContentsFromContainer(player, (Container) player.level().getBlockEntity(pos), 3);
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        // Cast is done to WorldlyContainer to handle Iron Furnaces
        WorldlyContainer furnace = (WorldlyContainer) player.level().getBlockEntity(pos);
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            if (slot != 1 || furnace.canPlaceItem(1, playerItem) || playerItem.isEmpty()) {
                SwapResult result = ImmersiveLogicHelpers.instance().swapItems(playerItem, furnaceItem, amount, player);
                result.giveToPlayer(player, hand);
                furnace.setItem(slot, result.immersiveStack());
                furnace.setChanged();
            }
        } else {
            SwapResult result = ImmersiveLogicHelpers.instance().swapItemsWithOutput(playerItem, furnaceItem, player);
            result.giveToPlayer(player, hand);
            furnace.setItem(2, result.immersiveStack());
            awardXP(furnace, player);
            furnace.setChanged();
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useFurnaceImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("furnace");
    }

    protected void awardXP(WorldlyContainer furnace, ServerPlayer player) {
        // Experience and recipes reward. Given only if at least one item was taken. This is the same as
        // vanilla behavior.
        if (furnace instanceof AbstractFurnaceBlockEntity afbe) {
            afbe.awardUsedRecipesAndPopExperience(player);
        }
    }
}
