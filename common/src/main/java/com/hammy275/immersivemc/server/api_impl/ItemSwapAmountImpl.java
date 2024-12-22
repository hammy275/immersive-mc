package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.PlacementMode;

public class ItemSwapAmountImpl implements ItemSwapAmount {

    private final PlacementMode placementMode;
    private final int numPlacements;

    public ItemSwapAmountImpl(PlacementMode placementMode, int numPlacements) {
        this.placementMode = placementMode;
        this.numPlacements = numPlacements;
    }

    @Override
    public int getNumItemsToSwap(int stackSize) {
        return switch (placementMode) {
            case SINGLE -> 1;
            case SPLIT -> stackSize / numPlacements;
        };
    }
}
