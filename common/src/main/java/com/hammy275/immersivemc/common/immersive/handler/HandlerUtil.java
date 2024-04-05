package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HandlerUtil {
    public static ListOfItemsStorage makeInventoryContentsFromContainer(ServerPlayer player, Container container, int maxItems) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < maxItems; i++) {
            items.add(container.getItem(i));
        }
        return new ListOfItemsStorage(items, maxItems);
    }
}
