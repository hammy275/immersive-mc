package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ChestLikeHandler implements ImmersiveHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>();
        Container inv;
        Container lootrInv = Lootr.lootrImpl.getContainer(player, pos);
        if (lootrInv != null) {
            inv = lootrInv;
        } else {
            inv = (Container) player.level.getBlockEntity(pos);
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            items.add(inv.getItem(i));
        }
        return new ListOfItemsStorage(items, inv.getContainerSize());
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new ListOfItemsStorage();
    }

    public boolean canPlaceItem(ItemStack item) {
        return true;
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        Container container = (Container) player.level.getBlockEntity(pos);
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
    public boolean usesWorldStorage() {
        return false;
    }
}
