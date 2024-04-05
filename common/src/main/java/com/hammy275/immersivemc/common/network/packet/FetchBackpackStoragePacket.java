package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FetchBackpackStoragePacket {

    public final List<ItemStack> backpackCraftingItems;


    public FetchBackpackStoragePacket() {
        backpackCraftingItems = null;
    }

    public FetchBackpackStoragePacket(List<ItemStack> items) {
        this.backpackCraftingItems = items;
    }

    public static void encode(FetchBackpackStoragePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.backpackCraftingItems != null);
        if (packet.backpackCraftingItems != null) {
            buffer.writeInt(packet.backpackCraftingItems.size());
            packet.backpackCraftingItems.forEach(buffer::writeItem);
        }
    }

    public static FetchBackpackStoragePacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            int numItems = buffer.readInt();
            List<ItemStack> items = new ArrayList<>(numItems);
            for (int i = 0; i < numItems; i++) {
                items.add(buffer.readItem());
            }
            return new FetchBackpackStoragePacket(items);
        } else {
            return new FetchBackpackStoragePacket(null);
        }
    }

    public static void handle(FetchBackpackStoragePacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // Server to client
                handleClient(message);
            } else { // Client to server
                Network.INSTANCE.sendToPlayer(player,
                        new FetchBackpackStoragePacket(ImmersiveMCPlayerStorages.getBackpackCraftingStorage(player)));
            }
        });
    }

    public static void handleClient(FetchBackpackStoragePacket message) {
        Immersives.immersiveBackpack.processFromNetwork(message.backpackCraftingItems);
    }
}
