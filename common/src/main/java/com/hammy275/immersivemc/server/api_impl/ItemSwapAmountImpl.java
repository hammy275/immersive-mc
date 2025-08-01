package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;

public class ItemSwapAmountImpl implements ItemSwapAmount {

    private final SwapMode swapMode;
    private final int numPlacements;
    private final int handStackSize;
    private final int slotIndex;

    public ItemSwapAmountImpl(SwapMode swapMode, int numPlacements, int handStackSize, int slotIndex) {
        this.swapMode = swapMode;
        this.numPlacements = numPlacements;
        this.handStackSize = handStackSize;
        this.slotIndex = slotIndex;
    }

    @Override
    public int getNumItemsToSwap() {
        return switch (swapMode) {
            case SINGLE -> 1;
            case SPLIT -> {
                int amount = handStackSize / numPlacements;
                int leftover = handStackSize % numPlacements;
                if (leftover > 0 && slotIndex < leftover) {
                    amount++;
                }
                yield amount;
            }
            case ALL -> this.handStackSize;
            case CONSTANT -> throw new IllegalArgumentException("Attempted to swap with CONSTANT type from ItemSwapAmountImpl.");
        };
    }

    @Override
    public SwapMode getSwapMode() {
        return this.swapMode;
    }
}
