package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.network.FriendlyByteBuf;

public class NullStorage implements NetworkStorage {
    @Override
    public void encode(FriendlyByteBuf buffer) {

    }

    @Override
    public void decode(FriendlyByteBuf buffer) {

    }
}
