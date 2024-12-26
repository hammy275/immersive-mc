package com.hammy275.immersivemc.common.config;

public enum PlacementMode {
    SINGLE,
    SPLIT;

    public static PlacementMode fromInt(int ordinal) {
        return PlacementMode.values()[ordinal];
    }
}
