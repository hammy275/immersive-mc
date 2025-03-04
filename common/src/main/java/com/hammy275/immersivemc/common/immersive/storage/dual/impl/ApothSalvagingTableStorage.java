package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class ApothSalvagingTableStorage extends ItemStorage {
    public ApothSalvagingTableStorage() {
        super(9, 8);
    }

    @Override
    public WorldStorageHandler<? extends NetworkStorage> getHandler() {
        return ImmersiveHandlers.apothSalvagingTableHandler;
    }
}
