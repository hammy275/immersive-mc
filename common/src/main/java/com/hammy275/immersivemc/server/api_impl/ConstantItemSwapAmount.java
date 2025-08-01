package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;

public class ConstantItemSwapAmount implements ItemSwapAmount {

    private final int amountToSwap;

    public ConstantItemSwapAmount(int amountToSwap) {
        this.amountToSwap = amountToSwap;
    }

    @Override
    public int getNumItemsToSwap() {
        return amountToSwap;
    }

    @Override
    public SwapMode getSwapMode() {
        return SwapMode.CONSTANT;
    }
}
