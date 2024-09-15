package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * An object that can be written into and out of a buffer. This is used by {@link ImmersiveHandler} to transfer
 * Immersive storage contents between the server and the client.
 */
public interface NetworkStorage {

    /**
     * Encode this storage into a buffer.
     * @param buffer Buffer to encode storage into.
     */
    void encode(RegistryFriendlyByteBuf buffer);

    /**
     * Decode the buffer into this object.
     * @param buffer Buffer to decode from.
     */
    void decode(RegistryFriendlyByteBuf buffer);
}
