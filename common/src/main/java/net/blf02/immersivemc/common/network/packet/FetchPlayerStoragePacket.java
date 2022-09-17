package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import dev.architectury.networking.NetworkManager;

import java.util.function.Supplier;

public class FetchPlayerStoragePacket {

    public final String type;

    public final ImmersiveStorage storage;

    public FetchPlayerStoragePacket(String type) {
        this.type = type;
        this.storage = null;
    }

    public FetchPlayerStoragePacket(ImmersiveStorage storage, String storageType) {
        this.storage = storage;
        this.type = storageType;
    }

    public boolean isRequest() {
        return this.storage == null;
    }

    public static void encode(FetchPlayerStoragePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isRequest());
        if (packet.isRequest()) {
            buffer.writeUtf(packet.type);
        } else {
            buffer.writeNbt(packet.storage.save(new CompoundTag()));
            buffer.writeUtf(packet.type);
        }
    }

    public static FetchPlayerStoragePacket decode(FriendlyByteBuf buffer) {
        boolean isRequest = buffer.readBoolean();
        if (isRequest) {
            return new FetchPlayerStoragePacket(buffer.readUtf());
        } else {
            CompoundTag storageTag = buffer.readNbt();
            String storageType = buffer.readUtf();
            return new FetchPlayerStoragePacket(GetStorage.assembleStorage(storageTag,
                    ImmersiveStorage.TYPE, null), storageType);
        }
    }

    public static void handle(FetchPlayerStoragePacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // Server to client
                handleClient(message);
            } else { // Client to server
                ImmersiveStorage storage = GetStorage.getPlayerStorage(player, message.type);
                Network.INSTANCE.sendToPlayer(player,
                        new FetchPlayerStoragePacket(storage, message.type));
            }
        });
    }

    public static void handleClient(FetchPlayerStoragePacket message) {
        if (message.type.equals("backpack")) {
            Immersives.immersiveBackpack.processFromNetwork(message.storage);
        }
    }
}
