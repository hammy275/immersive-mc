package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class ContainerHandler<S extends NetworkStorage> implements BlockBasedImmersiveHandler<S> {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos) {
        return DirtyTracker.isDirty(tracker.level(), pos);
    }

    @Override
    public boolean clientAuthoritative() {
        return false;
    }
}
