package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;

public interface BuiltImmersive<E, S extends NetworkStorage> extends Immersive<BuiltImmersiveInfo<E>, S> {

    /**
     * Creates a clone of this Immersive for creating an Immersive similar to this one.
     * @param newHandler The new handler for this Immersive.
     * @return A new builder instance.
     */
    public <T extends NetworkStorage> ImmersiveBuilder<E, T> getBuilderClone(ImmersiveHandler<T> newHandler);
}
