package com.hammy275.immersivemc.server.storage.world.impl;

import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;

public class ETableWorldStorage extends ItemStorage {
    public ETableWorldStorage() {
        super(1, 0);
    }

    @Override
    public WorldStorageHandler<ETableStorage> getHandler() {
        return ImmersiveHandlers.enchantingTableHandler;
    }
}
