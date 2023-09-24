package com.hammy275.immersivemc.common.config;

public enum ReachBehindBackpackMode {
    BEHIND_BACK,
    OVER_SHOULDER,
    BOTH,
    NONE;

    public boolean usesOverShoulder() {
        return this == OVER_SHOULDER || this == BOTH;
    }

    public boolean usesBehindBack() {
        return this == BEHIND_BACK || this == BOTH;
    }
}
