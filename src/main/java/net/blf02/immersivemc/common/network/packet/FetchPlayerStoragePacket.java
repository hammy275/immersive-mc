package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.ImmersiveBackpack;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

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

    public static void encode(FetchPlayerStoragePacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.isRequest());
        if (packet.isRequest()) {
            buffer.writeUtf(packet.type);
        } else {
            buffer.writeNbt(packet.storage.save(new CompoundNBT()));
            buffer.writeUtf(packet.type);
        }
    }

    public static FetchPlayerStoragePacket decode(PacketBuffer buffer) {
        boolean isRequest = buffer.readBoolean();
        if (isRequest) {
            return new FetchPlayerStoragePacket(buffer.readUtf());
        } else {
            CompoundNBT storageTag = buffer.readNbt();
            String storageType = buffer.readUtf();
            return new FetchPlayerStoragePacket(GetStorage.assembleStorage(storageTag,
                    ImmersiveStorage.TYPE, null), storageType);
        }
    }

    public static void handle(FetchPlayerStoragePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) { // Server to client
                handleClient(message);
            } else { // Client to server
                ImmersiveStorage storage = GetStorage.getPlayerStorage(player, message.type);
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new FetchPlayerStoragePacket(storage, message.type));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handleClient(FetchPlayerStoragePacket message) {
        if (message.type.equals("backpack")) {
            ImmersiveBackpack.singleton.processFromNetwork(message.storage);
        }
    }
}
