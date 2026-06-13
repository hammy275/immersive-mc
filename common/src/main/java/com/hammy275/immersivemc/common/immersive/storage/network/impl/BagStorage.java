package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BagStorage extends ListOfItemsStorage {

    public boolean useSwappedHands;

    public BagStorage() {
        super();
    }

    public BagStorage(List<ItemStack> items, boolean useSwappedHands) {
        super(items);
        this.useSwappedHands = useSwappedHands;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeBoolean(useSwappedHands);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        this.useSwappedHands = buffer.readBoolean();
    }
}
