package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import net.minecraft.network.FriendlyByteBuf;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BagStorage extends ListOfItemsStorage {

    public boolean useSwappedHands;
    public int bagColor;
    public BackpackMode bagMode;

    public BagStorage() {
        super();
    }

    public BagStorage(List<ItemStack> items, ClientActiveConfig playerConfig) {
        super(items);
        this.useSwappedHands = playerConfig.swapBagHand;
        this.bagColor = playerConfig.bagColor;
        this.bagMode = playerConfig.bagMode;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeBoolean(useSwappedHands);
        buffer.writeInt(bagColor);
        buffer.writeEnum(bagMode);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        super.decode(buffer);
        this.useSwappedHands = buffer.readBoolean();
        this.bagColor = buffer.readInt();
        this.bagMode = buffer.readEnum(BackpackMode.class);
    }
}
