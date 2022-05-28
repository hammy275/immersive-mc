package net.blf02.immersivemc.client.storage;

import net.blf02.immersivemc.common.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ClientStorage {

    // Is only used for handling recipes, so setting this to null is fine
    public static ItemStack[] craftingStorage = new ItemStack[]{
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
    };
    public static ItemStack craftingOutput = ItemStack.EMPTY;

    // Left, right, output
    public static final ItemStack[] anvilStorage = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public static int anvilCost = -1;
    public static final ItemStack[] smithingStorage = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    // ETable
    public static final ETableInfo weakInfo = new ETableInfo();
    public static final ETableInfo midInfo = new ETableInfo();
    public static final ETableInfo strongInfo = new ETableInfo();
    public static ItemStack eTableItem = ItemStack.EMPTY;
    public static ItemStack eTableEnchCopy = ItemStack.EMPTY;

    public static void removeLackingIngredientsFromTable(PlayerEntity player) {
        if (player.abilities.instabuild) return;
        List<ItemStack> stacks = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        // Set stacks and amounts to the stacks and amounts needed of each item
        for (ItemStack toCount : craftingStorage) {
            boolean merged = false;
            // Check if an item in the table matches one from before. If so, we add it to the running total in `amounts`.
            // Otherwise, add it newly to `stacks`, and a 1 to `amounts` for it.
            for (int countedNum = 0; countedNum < stacks.size(); countedNum++) {
                if (Util.stacksEqualBesidesCount(toCount, stacks.get(countedNum))) {
                    amounts.set(countedNum, amounts.get(countedNum) + 1);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                stacks.add(toCount);
                amounts.add(1);
            }
        }

        // Queue for removal all items from the table that the player doesn't have enough of
        List<ItemStack> toRemove = new LinkedList<>();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack toCheck = stacks.get(i);
            int needed = amounts.get(i);
            int currentAmount = -needed; // The client doesn't yet know we just took the items for crafting, so simulate that here.
            boolean shouldRemoveFromTable = true;
            for (ItemStack invItem : player.inventory.items) {
                if (Util.stacksEqualBesidesCount(toCheck, invItem)) {
                    currentAmount += invItem.getCount();
                    if (currentAmount >= needed) {
                        shouldRemoveFromTable = false;
                        break;
                    }
                }
            }
            if (shouldRemoveFromTable) {
                toRemove.add(toCheck);
            }
        }
        // Do the aforementioned removing
        for (ItemStack s : toRemove) {
            for (int i = 0; i < 9; i++) {
                if (!s.isEmpty() && Util.stacksEqualBesidesCount(s, craftingStorage[i])) {
                    craftingStorage[i] = ItemStack.EMPTY;
                    craftingOutput = ItemStack.EMPTY; // Clear output if we remove something from input
                }
            }
        }

    }

    public static void resetEnchs() {
        weakInfo.textPreview = null;
        midInfo.textPreview = null;
        strongInfo.textPreview = null;
        eTableItem = ItemStack.EMPTY;
        eTableEnchCopy = ItemStack.EMPTY;
    }

    public static class ETableInfo {
        public int levelsNeeded;
        public ITextComponent textPreview = null;

        public boolean isPresent() {
            return this.textPreview != null;
        }
    }

}
