package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;

/**
 * Representation of a built Immersive made using an {@link ImmersiveBuilder}. This acts as an {@link Immersive}, but
 * can also be used to make new {@link ImmersiveBuilder}s that start as copies of this built Immersive.
 * @param <E> The type of the "extra data" stored on info instances of this Immersive.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 */
public interface BuiltImmersive<E, S extends NetworkStorage> extends Immersive<BuiltImmersiveInfo<E>, S> {

    /**
     * Creates a clone of this Immersive for creating an Immersive similar to this one.
     * @param newHandler The new handler for this Immersive.
     * @return A new builder instance.
     */
    public <T extends NetworkStorage> ImmersiveBuilder<E, T> getBuilderClone(ImmersiveHandler<T> newHandler);
}
