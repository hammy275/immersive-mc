package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ChestStorage implements NetworkStorage {

    public final Map<BlockPos, ListOfItemsStorage> items = new HashMap<>();

    public ChestStorage() {

    }

    public ChestStorage(BlockPos pos, ListOfItemsStorage items) {
        this.items.put(pos, items);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(items.size());
        for (Map.Entry<BlockPos, ListOfItemsStorage> entry : items.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            entry.getValue().encode(buffer);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            ListOfItemsStorage itemsStorage = new ListOfItemsStorage();
            itemsStorage.decode(buffer);
            items.put(pos, itemsStorage);
        }
    }
}
