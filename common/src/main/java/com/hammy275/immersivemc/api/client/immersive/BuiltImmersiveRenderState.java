package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.BlockPos;

public interface BuiltImmersiveRenderState<ER> extends ImmersiveRenderState {

    public BlockPos getBlockPos();

    public ER getExtraRenderData();
}
