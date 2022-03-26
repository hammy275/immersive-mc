package net.blf02.immersivemc.common.swap;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.AbstractList;
import java.util.Optional;

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

    public static void handleCrafting(ServerPlayerEntity player, CraftingInventory inv,
                                      BlockPos tablePos) {
        Optional<ICraftingRecipe> res = player.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING,
                inv, player.level);
        if (res.isPresent()) {
            if (removeNeededIngredients(player, inv)) {
                // Give our item to us
                ItemStack stackOut = res.get().assemble(inv);
                BlockPos pos = tablePos.above();
                ItemEntity entOut = new ItemEntity(player.level, pos.getX(), pos.getY(), pos.getZ());
                entOut.setItem(stackOut);
                entOut.setDeltaMovement(0, 0, 0);
                player.level.addFreshEntity(entOut);
            }
        }
    }

    protected static boolean removeNeededIngredients(ServerPlayerEntity player, CraftingInventory inv) {
        if (player.isCreative()) return true; // Always succeed if in creative mode

        NonNullList<ItemStack> toRemoves = NonNullList.create();
        for (int i = 0; i < 9; i++) {
            toRemoves.add(inv.getItem(i));
        }

        if (!doAndSimulateRemove(false, toRemoves, player.inventory.items)) {
            return false;
        }
        doAndSimulateRemove(true, toRemoves, player.inventory.items);
        return true;
    }

    /**
     * Checks/actually removes items from an inventory
     * @param doRemoval Whether to actually remove the items from the inventory or not
     * @param toRemoves List with items to remove/check. The "recipe".
     * @param inventory List of items to remove from/check. The "inventory"
     * @return true if all removals can be done or were done. false otherwise
     */
    private static boolean doAndSimulateRemove(boolean doRemoval, AbstractList<ItemStack> toRemoves,
                                               AbstractList<ItemStack> inventory) {
        if (!doRemoval) {
            NonNullList<ItemStack> inventoryClone = NonNullList.create();
            for (ItemStack s : inventory) {
                inventoryClone.add(s.copy());
            }
            inventory = inventoryClone;
        }

        for (ItemStack toRemove : toRemoves) {
            boolean didRemoval = false;
            if (toRemove.isEmpty()) continue; // We can always remove nothingness, and we don't actually remove it

            // Error out if item count isn't exactly 1 (this function depends on it)
            if (toRemove.getCount() != 1) throw new IllegalArgumentException("Must remove exactly 1 item.");

            for (ItemStack invItem : inventory) {
                if (invItem.isEmpty()) continue; // No way we can equal an empty item
                boolean tagsBothNullOrNot = invItem.hasTag() == toRemove.hasTag();
                boolean sameBaseItem = invItem.getItem() == toRemove.getItem();
                boolean equalTags;
                if (tagsBothNullOrNot) {
                    if (invItem.hasTag()) {
                        equalTags = invItem.getTag().equals(toRemove.getTag()); // Actually compare tags
                    } else {
                        equalTags = true; // Both items have no tags
                    }
                } else {
                    equalTags = false; // One item tag is null, the other isn't
                }
                if (tagsBothNullOrNot && sameBaseItem && equalTags) {
                    invItem.shrink(1);
                    didRemoval = true;
                    break;
                }
            }
            if (!didRemoval) {
                return false;
            }
        }
        return true;
    }
}
