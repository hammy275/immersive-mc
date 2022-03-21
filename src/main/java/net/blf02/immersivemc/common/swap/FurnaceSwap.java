package net.blf02.immersivemc.common.swap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Hand;

public class FurnaceSwap {

    public static void handleSwap(AbstractFurnaceTileEntity furnace, PlayerEntity player,
                                  Hand hand, int slot) {
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            if (!furnace.canPlaceItem(slot, playerItem)) return;
            player.setItemInHand(hand, furnaceItem);
            furnace.setItem(slot, playerItem);
        } else {
            if (player.getItemInHand(hand) != ItemStack.EMPTY) return;
            player.setItemInHand(hand, furnaceItem);
            furnace.setItem(2, ItemStack.EMPTY);
        }

    }
}
