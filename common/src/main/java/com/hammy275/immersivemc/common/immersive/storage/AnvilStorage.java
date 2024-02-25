package com.hammy275.immersivemc.common.immersive.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AnvilStorage extends ListOfItemsStorage {

    public int xpLevels = 0;

    public AnvilStorage(List<ItemStack> items, int xpLevels) {
        super(items, 3);
        this.xpLevels = xpLevels;
    }

    public AnvilStorage() {
        super();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeInt(this.xpLevels);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.xpLevels = buffer.readInt();
    }
}
