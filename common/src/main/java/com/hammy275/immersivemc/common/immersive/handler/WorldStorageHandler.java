package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
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
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage storage = GetStorage.getStorageIfExists(player, pos);
        if (storage != null) {
            storage.returnItems(player);
            GetStorage.updateStorageOutputAfterItemReturn(player, pos, storage);
        }
    }

    public abstract ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos);
}
