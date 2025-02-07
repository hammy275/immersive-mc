package com.hammy275.immersivemc.common.config;

public enum CrouchMode {
    SWAP_ALL,
    BYPASS_IMMERSIVE,
    NONE;

    public boolean swapAll() {
        return this == SWAP_ALL;
    }

    public boolean bypassImmersive() {
        return this == BYPASS_IMMERSIVE;
    }
}
