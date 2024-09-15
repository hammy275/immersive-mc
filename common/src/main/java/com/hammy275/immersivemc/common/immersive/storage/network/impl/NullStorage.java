package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class NullStorage implements NetworkStorage {
    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {

    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {

    }
}
