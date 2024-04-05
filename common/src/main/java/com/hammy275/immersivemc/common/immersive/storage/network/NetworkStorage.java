package com.hammy275.immersivemc.common.immersive.storage.network;

import net.minecraft.network.FriendlyByteBuf;

public interface NetworkStorage {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);
}
