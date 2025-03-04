package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class CraftingTableStorage extends ItemStorage {
    public CraftingTableStorage() {
        super(10, 8);
    }

    @Override
    public WorldStorageHandler<CraftingTableStorage> getHandler() {
        return ImmersiveHandlers.craftingHandler;
    }
}
