package com.hammy275.immersivemc.common.config;

public enum PlacementMode {
    PLACE_ONE,
    PLACE_QUARTER,
    PLACE_HALF,
    PLACE_ALL;

    public static PlacementMode fromInt(int ordinal) {
        return PlacementMode.values()[ordinal];
    }
}
