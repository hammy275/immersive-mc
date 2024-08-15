package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListOfItemsStorage implements NetworkStorage {

    private final List<ItemStack> items = new ArrayList<>();

    public ListOfItemsStorage(List<ItemStack> items, int maxItems) {
        for (int i = 0; i < Math.min(items.size(), maxItems); i++) {
            this.items.add(items.get(i));
        }
    }

    public ListOfItemsStorage(List<ItemStack> items) {
        this(items, items.size());
    }

    public ListOfItemsStorage(ItemStack... items) {
        this(Arrays.asList(items), items.length);
    }

    public ListOfItemsStorage() {
        this(Collections.emptyList(), 0);
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.items.size());
        for (ItemStack item : this.items) {
            buffer.writeItem(item);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.items.clear();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            this.items.add(buffer.readItem());
        }
    }
}
