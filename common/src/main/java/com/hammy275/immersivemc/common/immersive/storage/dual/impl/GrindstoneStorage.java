package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;

public class GrindstoneStorage extends ItemStorage {
    public GrindstoneStorage() {
        super(3, 1);
    }

    @Override
    public WorldStorageHandler<? extends NetworkStorage> getHandler() {
        return ImmersiveHandlers.grindstoneHandler;
    }
}
