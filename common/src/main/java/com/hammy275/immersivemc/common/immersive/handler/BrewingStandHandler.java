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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public class BrewingStandHandler extends ContainerHandler<ListOfItemsStorage> {
    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer tracker, BlockPos pos) {
        return HandlerUtil.makeInventoryContentsFromContainer(tracker, (Container) tracker.level().getBlockEntity(pos), 5);
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount) {
        Container stand = (Container) tracker.level().getBlockEntity(pos);
        ItemStack standItem = stand.getItem(slot).copy();
        ItemStack playerItem = tracker.getItemInHand(hand).copy();
        if (slot < 3) { // Potions
            if (!stand.canPlaceItem(slot, playerItem) && !playerItem.isEmpty()
                    && !(standItem.getItem() instanceof PotionItem)) return;
            tracker.setItemInHand(hand, standItem);
            stand.setItem(slot, playerItem);
        } else { // Ingredient and Fuel
            if (!stand.canPlaceItem(slot, playerItem) && !playerItem.isEmpty()) return;
            SwapResult result = ImmersiveLogicHelpers.instance().swapItems(playerItem, standItem, amount, tracker);
            result.giveToPlayer(tracker, hand);
            stand.setItem(slot, result.immersiveStack());
        }
        stand.setChanged();
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof BrewingStandBlockEntity;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useBrewingStandImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("brewing_stand");
    }
}
