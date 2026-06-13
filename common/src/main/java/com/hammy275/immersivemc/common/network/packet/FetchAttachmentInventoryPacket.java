package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FetchAttachmentInventoryPacket<S extends NetworkStorage> {

    public final S storage;
    public final PlayerAttachmentImmersiveHandler<S> handler;
    public final UUID ownerUUID;

    public FetchAttachmentInventoryPacket(PlayerAttachmentImmersiveHandler<S> handler, S storage, UUID ownerUUID) {
        this.handler = handler;
        this.storage = storage;
        this.ownerUUID = ownerUUID;
    }

    public static <NS extends NetworkStorage> void encode(FetchAttachmentInventoryPacket<NS> packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(packet.ownerUUID);
        buffer.writeIdentifier(packet.handler.getID());
        packet.storage.encode(buffer);
    }

    @SuppressWarnings("unchecked")
    public static <NS extends NetworkStorage> FetchAttachmentInventoryPacket<NS> decode(RegistryFriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        PlayerAttachmentImmersiveHandler<NS> handlerToSet = null;
        NS storage = null;
        Identifier id = buffer.readIdentifier();
        for (PlayerAttachmentImmersiveHandler<?> handler : ImmersiveHandlers.ATTACHMENT_HANDLERS) {
            if (handler.getID().equals(id)) {
                handlerToSet = (PlayerAttachmentImmersiveHandler<NS>) handler;
                storage = handlerToSet.getEmptyNetworkStorage();
                storage.decode(buffer);
                break;
            }
        }
        if (storage == null) {
            throw new IllegalArgumentException("ID " + id + " not found!");
        }
        return new FetchAttachmentInventoryPacket<>(handlerToSet, storage, uuid);
    }

    public static <NS extends NetworkStorage> void handle(final FetchAttachmentInventoryPacket<NS> message, ServerPlayer player) {
        if (player == null) {
            NetworkClientHandlers.handleReceiveInvData(message.storage, message.ownerUUID, message.handler);
        }
    }

}
