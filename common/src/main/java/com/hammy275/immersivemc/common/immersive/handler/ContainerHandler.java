package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class ContainerHandler<S extends NetworkStorage> implements ImmersiveHandler<S> {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return DirtyTracker.isDirty(player.level(), pos);
    }

    @Override
    public boolean clientAuthoritative() {
        return false;
    }
}
