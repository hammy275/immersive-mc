package com.hammy275.immersivemc.common.config;

public enum PlacementMode {
    SINGLE,
    SPLIT;

    public int toInt() {
        return this.ordinal();
    }

    public static PlacementMode fromInt(int ordinal) {
        return PlacementMode.values()[ordinal];
    }
}
