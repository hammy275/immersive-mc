package com.hammy275.immersivemc.api.server;

/**
 * Encapsulates the data used to determine how many items are being swapped for this swap.
 */
public interface ItemSwapAmount {

    /**
     * Get the number of items to swap based on the input item stack size.
     * @param stackSize The size of the item stack being swapped from the player.
     * @return The amount of the stack to ideally swap.
     */
    public int getNumItemsToSwap(int stackSize);
}
