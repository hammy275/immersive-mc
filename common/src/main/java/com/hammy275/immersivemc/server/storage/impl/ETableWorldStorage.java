package com.hammy275.immersivemc.server.storage.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class ETableWorldStorage extends ItemWorldStorage {
    public ETableWorldStorage() {
        super(1, 1);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.enchantingTableHandler;
    }
}
