package com.hammy275.immersivemc.api.common.immersive;

/**
 * The type of swap that should be performed. Found as part of
 * {@link ItemSwapAmount} for how many items to swap into and/or out of an
 * Immersive.
 */
public enum SwapMode {
    /**
     * Only swap one item.
     */
    SINGLE,
    /**
     * Swap the held stack, split evenly into multiple slots.
     */
    SPLIT,
    /**
     * Swap as many items as possible.
     */
    ALL,
    /**
     * Swap a constant number of items.
     */
    CONSTANT
}
