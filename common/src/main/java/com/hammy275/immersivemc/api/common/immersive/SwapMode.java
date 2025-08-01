package com.hammy275.immersivemc.api.common.immersive;

/**
 * The type of swap that should be performed. Found as part of
 * {@link ItemSwapAmount} for how many items to swap into and/or out of an
 * Immersive.
 */
public enum SwapMode {
    SINGLE, // Only swap one item
    SPLIT, // Swap the held stack, split evenly into multiple slots
    ALL, // Swap as many items as possible
    CONSTANT // Swap a constant number of items
}
