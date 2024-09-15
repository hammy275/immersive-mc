package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FetchBackpackStoragePacket {

    public final List<ItemStack> backpackCraftingItems;


    public FetchBackpackStoragePacket() {
        backpackCraftingItems = null;
    }

    public FetchBackpackStoragePacket(List<ItemStack> items) {
        this.backpackCraftingItems = items;
    }

    public static void encode(FetchBackpackStoragePacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.backpackCraftingItems != null);
        if (packet.backpackCraftingItems != null) {
            buffer.writeInt(packet.backpackCraftingItems.size());
            packet.backpackCraftingItems.forEach(item -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, item));
        }
    }

    public static FetchBackpackStoragePacket decode(RegistryFriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            int numItems = buffer.readInt();
            List<ItemStack> items = new ArrayList<>(numItems);
            for (int i = 0; i < numItems; i++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            }
            return new FetchBackpackStoragePacket(items);
        } else {
            return new FetchBackpackStoragePacket(null);
        }
    }

    public static void handle(FetchBackpackStoragePacket message, ServerPlayer player) {
        if (player == null) { // Server to client
            handleClient(message);
        } else { // Client to server
            Network.INSTANCE.sendToPlayer(player,
                    new FetchBackpackStoragePacket(ImmersiveMCPlayerStorages.getBackpackCraftingStorage(player)));
        }
    }

    public static void handleClient(FetchBackpackStoragePacket message) {
        Immersives.immersiveBackpack.processFromNetwork(message.backpackCraftingItems);
    }
}
