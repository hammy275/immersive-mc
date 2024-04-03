package com.hammy275.immersivemc.server.storage.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class SmithingTableWorldStorage extends ItemWorldStorage {
    public SmithingTableWorldStorage() {
        super(4, 2);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.smithingTableHandler;
    }
}
