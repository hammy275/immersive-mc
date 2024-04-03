package com.hammy275.immersivemc.server.storage.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class BeaconWorldStorage extends ItemWorldStorage {
    public BeaconWorldStorage() {
        super(1, 0);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.beaconHandler;
    }
}
