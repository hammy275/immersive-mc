package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.PlacementMode;

public class ItemSwapAmountImpl implements ItemSwapAmount {

    private final PlacementMode placementMode;
    private final int numPlacements;
    private final int handStackSize;
    private final int slotIndex;

    public ItemSwapAmountImpl(PlacementMode placementMode, int numPlacements, int handStackSize, int slotIndex) {
        this.placementMode = placementMode;
        this.numPlacements = numPlacements;
        this.handStackSize = handStackSize;
        this.slotIndex = slotIndex;
    }

    @Override
    public int getNumItemsToSwap() {
        return switch (placementMode) {
            case SINGLE -> 1;
            case SPLIT -> {
                int amount = handStackSize / numPlacements;
                int leftover = handStackSize % numPlacements;
                if (leftover > 0 && slotIndex < leftover) {
                    amount++;
                }
                yield amount;
            }
        };
    }
}
