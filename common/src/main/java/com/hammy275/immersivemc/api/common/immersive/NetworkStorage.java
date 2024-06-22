package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.network.FriendlyByteBuf;

/**
 * An object that can be written into and out of a buffer. This is used by {@link ImmersiveHandler} to transfer
 * Immersive storage contents between the server and the client.
 */
public interface NetworkStorage {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);
}
