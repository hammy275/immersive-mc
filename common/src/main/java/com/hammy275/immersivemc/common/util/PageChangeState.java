package com.hammy275.immersivemc.common.util;

public enum PageChangeState {
    LEFT_TO_RIGHT(false), RIGHT_TO_LEFT(false), NONE(false),
    LEFT_TO_RIGHT_ANIM(true), RIGHT_TO_LEFT_ANIM(true);

    public final boolean isAnim;

    PageChangeState(boolean isAnim) {
        this.isAnim = isAnim;
    }
}
