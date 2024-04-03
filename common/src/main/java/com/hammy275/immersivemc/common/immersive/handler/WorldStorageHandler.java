package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.server.storage.WorldStorage;

public interface WorldStorageHandler extends ImmersiveHandler {

    /**
     * @return An empty WorldStorage to load from NBT in.
     */
    public WorldStorage getEmptyWorldStorage();

    /**
     * @return The class this handler's world storage uses.
     */
    public Class<? extends WorldStorage> getWorldStorageClass();

}
