package com.hammy275.immersivemc.common.immersive.storage;

import net.minecraft.network.FriendlyByteBuf;

public interface HandlerStorage {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);
}
