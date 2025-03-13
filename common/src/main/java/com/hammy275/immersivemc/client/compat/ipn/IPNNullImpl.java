package com.hammy275.immersivemc.client.compat.ipn;

public class IPNNullImpl implements IPNCompat {
    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void doInventorySwap(int a, int b) {

    }
}
