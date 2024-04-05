package com.hammy275.immersivemc.server.storage.world.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;

public class ETableWorldStorage extends ItemStorage {
    public ETableWorldStorage() {
        super(1, 1);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.enchantingTableHandler;
    }
}
