package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class BeaconStorage extends ItemStorage {
    public BeaconStorage() {
        super(1, 0);
    }

    @Override
    public WorldStorageHandler<BeaconStorage> getHandler() {
        return ImmersiveHandlers.beaconHandler;
    }
}
