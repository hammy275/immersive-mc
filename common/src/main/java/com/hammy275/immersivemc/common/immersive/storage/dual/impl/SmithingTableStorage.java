package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class SmithingTableStorage extends ItemStorage {
    public SmithingTableStorage() {
        super(4, 2);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.smithingTableHandler;
    }
}
