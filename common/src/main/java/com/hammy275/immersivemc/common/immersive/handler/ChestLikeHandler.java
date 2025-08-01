package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public abstract class ChestLikeHandler extends ContainerHandler<ListOfItemsStorage> {

    // NOTE: Inheritors should && with isValidBlock() if they might be a Lootr block!
    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return !Lootr.lootrImpl.isLootrContainer(pos, level) || !Lootr.lootrImpl.disableLootrContainerCompat();
    }

    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>();
        Container inv;
        Container lootrInv = Lootr.lootrImpl.getContainer(player, pos);
        if (lootrInv != null) {
            inv = lootrInv;
        } else {
            inv = (Container) player.level().getBlockEntity(pos);
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            items.add(inv.getItem(i));
        }
        return new ListOfItemsStorage(items, inv.getContainerSize());
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        if (Lootr.lootrImpl.isLootrContainer(pos, player.level())) {
            // Combine old ImmersiveMC "send data every-other tick" with if it's open, since contents can only
            // change if it's opened by the player.
            return Lootr.lootrImpl.isOpen(pos, player) && player.tickCount % 2 == 0;
        } else {
            return super.isDirtyForClientSync(player, pos);
        }
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    public boolean canPlaceItem(ItemStack item) {
        return true;
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        Container container = (Container) player.level().getBlockEntity(pos);
        Container lootrInv = Lootr.lootrImpl.getContainer(player, pos);
        if (lootrInv != null) {
            container = lootrInv;
        }
        ItemStack containerItem = container.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand);
        if (!canPlaceItem(playerItem)) {
            return;
        }
        if (playerItem.isEmpty() || containerItem.isEmpty() || !Util.stacksEqualBesidesCount(containerItem, playerItem)) {
            player.setItemInHand(hand, containerItem);
            container.setItem(slot, playerItem);
        } else {
            Util.ItemStackMergeResult result = Util.mergeStacks(containerItem, playerItem, false);
            player.setItemInHand(hand, result.mergedFrom);
            container.setItem(slot, result.mergedInto);
        }
        container.setChanged();
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        ChestToOpenSet.closeChest(player, pos);
    }
}
