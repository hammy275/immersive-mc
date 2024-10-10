package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.server.ItemSwapAmount;

public class ConstantItemSwapAmount implements ItemSwapAmount {

    private final int amountToSwap;

    public ConstantItemSwapAmount(int amountToSwap) {
        this.amountToSwap = amountToSwap;
    }

    @Override
    public int getNumItemsToSwap(int stackSize) {
        return amountToSwap;
    }
}
