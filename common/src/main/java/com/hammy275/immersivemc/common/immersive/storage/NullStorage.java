package com.hammy275.immersivemc.common.immersive.storage;

import net.minecraft.network.FriendlyByteBuf;

public class NullStorage implements HandlerStorage {
    @Override
    public void encode(FriendlyByteBuf buffer) {

    }

    @Override
    public void decode(FriendlyByteBuf buffer) {

    }
}
