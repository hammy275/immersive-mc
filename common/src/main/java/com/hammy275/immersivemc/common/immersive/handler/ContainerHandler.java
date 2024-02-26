package com.hammy275.immersivemc.common.immersive.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class ContainerHandler implements ImmersiveHandler {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return true; // TODO: Actually check if dirty or not.
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        // TODO: Actually clear flag checked for by isDirtyForClientSync
    }
}
