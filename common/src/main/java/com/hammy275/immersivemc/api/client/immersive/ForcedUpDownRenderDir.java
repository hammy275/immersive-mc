package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.Direction;

public enum ForcedUpDownRenderDir {

    UP(Direction.UP), DOWN(Direction.DOWN), NULL(null), NOT_FORCED(null);

    public final Direction direction;

    ForcedUpDownRenderDir(Direction dir) {
        this.direction = dir;
    }
}
