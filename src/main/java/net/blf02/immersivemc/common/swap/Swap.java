package net.blf02.immersivemc.common.swap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Hand;

public class Swap {

    public static void handleFurnaceSwap(AbstractFurnaceTileEntity furnace, PlayerEntity player,
                                         Hand hand, int slot) {
        ItemStack furnaceItem = furnace.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot != 2) {
            // TODO: Item merging
            if (!furnace.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY) return;
            player.setItemInHand(hand, furnaceItem);
            furnace.setItem(slot, playerItem);
        } else {
            if (playerItem == ItemStack.EMPTY) {
                player.setItemInHand(hand, furnaceItem);
                furnace.setItem(2, ItemStack.EMPTY);
            }
        }
    }

    public static void handleBrewingSwap(BrewingStandTileEntity stand, PlayerEntity player,
                                         Hand hand, int slot) {
        ItemStack standItem = stand.getItem(slot).copy();
        ItemStack playerItem = player.getItemInHand(hand).copy();
        if (slot < 3) { // Potions
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY
            && !(standItem.getItem() instanceof PotionItem)) return;
            player.setItemInHand(hand, standItem);
            stand.setItem(slot, playerItem);
        } else { // Ingredient and Fuel
            // TODO: Item merging
            if (!stand.canPlaceItem(slot, playerItem) && playerItem != ItemStack.EMPTY) return;
            player.setItemInHand(hand, standItem);
            stand.setItem(slot, playerItem);
        }
    }
}
