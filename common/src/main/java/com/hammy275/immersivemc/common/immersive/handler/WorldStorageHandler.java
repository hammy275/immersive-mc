package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;

public interface WorldStorageHandler<S extends NetworkStorage> extends ImmersiveHandler<S> {

    @Override
    default boolean clientAuthoritative() {
        return false;
    }

    /**
     * @return An empty WorldStorage to load from NBT in.
     */
    public WorldStorage getEmptyWorldStorage();

    /**
     * @return The class this handler's world storage uses.
     */
    public Class<? extends WorldStorage> getWorldStorageClass();

}
