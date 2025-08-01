package com.hammy275.immersivemc.api.common.immersive;

/**
 * Encapsulates the data used to determine how many items are being swapped for this swap.
 */
public interface ItemSwapAmount {

    /**
     * Get the number of items to swap based on the input item stack size.
     *
     * @return The amount of the stack to ideally swap.
     */
    public int getNumItemsToSwap();


    /**
     * Gets the underlying mode used for the swap. Useful for performing custom behavior instead of using
     * a {@code swapItems()} call in {@link com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers}.
     * @return The swap mode currently in use.
     */
    public SwapMode getSwapMode();
}
