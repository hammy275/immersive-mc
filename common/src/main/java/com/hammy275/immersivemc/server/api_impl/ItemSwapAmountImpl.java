package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.swap.Swap;

public class ItemSwapAmountImpl implements ItemSwapAmount {

    private final PlacementMode placementMode;

    public ItemSwapAmountImpl(PlacementMode placementMode) {
        this.placementMode = placementMode;
    }

    @Override
    public int getNumItemsToSwap(int stackSize) {
        return Swap.getPlaceAmount(stackSize, this.placementMode);
    }
}
