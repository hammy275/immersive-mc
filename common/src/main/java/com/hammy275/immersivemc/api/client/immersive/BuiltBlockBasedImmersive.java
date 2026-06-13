package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;

/**
 * Representation of a built Immersive made using an {@link BlockBasedImmersiveBuilder}. This acts as a
 * {@link BlockBasedImmersive}, but can also be used to make new {@link BlockBasedImmersiveBuilder}s that start as
 * copies of this built Immersive.
 * <p>
 * You should not implement this interface yourself! Instead, implement {@link BlockBasedImmersive} directly.
 * @param <E> The type of the "extra data" stored on info instances of this Immersive.
 * @param <ER> The type of the extracted render state for the "extra data" stored on info instances of this Immersive.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 */
public interface BuiltBlockBasedImmersive<E, ER, S extends NetworkStorage> extends BlockBasedImmersive<BuiltBlockBasedImmersiveInfo<E>, BuiltImmersiveRenderState<ER>, S> {

    /**
     * Creates a clone of this Immersive for creating an Immersive similar to this one.
     * @param newHandler The new handler for this Immersive.
     * @return A new builder instance.
     */
    public <T extends NetworkStorage> BlockBasedImmersiveBuilder<E, ER, T> getBuilderClone(BlockBasedImmersiveHandler<T> newHandler);
}
