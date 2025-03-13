package com.hammy275.immersivemc.client.compat.ipn;

import org.anti_ad.mc.ipn.api.access.IPN;

public class IPNCompatImpl implements IPNCompat {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public void doInventorySwap(int a, int b) {
        // Note: If swapping with the hotbar, b must be the hotbar slot!
        IPN.getInstance().getContainerClicker().swap(a, b);
    }
}
