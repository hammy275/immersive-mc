package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class WorldStorageHandler implements ImmersiveHandler {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return getStorage(player, pos).isDirtyForClientSync();
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        getStorage(player, pos).setNoLongerDirtyForClientSync();
    }

    @Override
    public void stopDirtyTracking(ServerPlayer player, BlockPos pos) {
        // NO-OP. WorldStorages easily track this already on their own.
    }

    public abstract ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos);
}
