package com.hammy275.immersivemc.server.storage.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class CraftingTableWorldStorage extends ItemWorldStorage {
    public CraftingTableWorldStorage() {
        super(10, 8);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.craftingHandler;
    }
}
