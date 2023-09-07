package com.hammy275.immersivemc.common.config;

public enum BackpackMode {
    BUNDLE(false), // Model provided by NahNotFox and is the default model
    BUNDLE_COLORABLE(true), // Above, but made grayscale so that it can be colored
    ORIGINAL(true), // Original bag model
    ORIGINAL_LOW_DETAIL(true); // Original low detail model

    public final boolean colorable;

    BackpackMode(boolean colorable) {
        this.colorable = colorable;
    }
}
