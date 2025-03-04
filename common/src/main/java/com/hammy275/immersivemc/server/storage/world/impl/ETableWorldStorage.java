package com.hammy275.immersivemc.server.storage.world.impl;

import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;

public class ETableWorldStorage extends ItemStorage {

    protected transient ApothStats lastApothStats = null;

    public ETableWorldStorage() {
        super(1, 0);
    }

    public void setDirtyFromApothStats(ApothStats newStats) {
        isDirtyForClientSync = isDirtyForClientSync || lastApothStats == null || !lastApothStats.equals(newStats);
        lastApothStats = newStats;
    }

    @Override
    public WorldStorageHandler<ETableStorage> getHandler() {
        return ImmersiveHandlers.enchantingTableHandler;
    }
}
