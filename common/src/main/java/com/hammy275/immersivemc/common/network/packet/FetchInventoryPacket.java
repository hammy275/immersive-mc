package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class FetchInventoryPacket<S extends NetworkStorage> {

    public final S storage;
    public final BlockBasedImmersiveHandler<S> handler;
    public final BlockPos pos;

    public FetchInventoryPacket(BlockBasedImmersiveHandler<S> handler, S storage, BlockPos pos) {
        this.handler = handler;
        this.storage = storage;
        this.pos = pos;
    }

    public static <NS extends NetworkStorage> void encode(FetchInventoryPacket<NS> packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeIdentifier(packet.handler.getID());
        packet.storage.encode(buffer);
    }

    @SuppressWarnings("unchecked")
    public static <NS extends NetworkStorage> FetchInventoryPacket<NS> decode(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockBasedImmersiveHandler<NS> handlerToSet = null;
        NS storage = null;
        Identifier id = buffer.readIdentifier();
        for (BlockBasedImmersiveHandler<?> handler : ImmersiveHandlers.HANDLERS) {
            if (handler.getID().equals(id)) {
                handlerToSet = (BlockBasedImmersiveHandler<NS>) handler;
                storage = handlerToSet.getEmptyNetworkStorage();
                storage.decode(buffer);
                break;
            }
        }
        if (storage == null) {
            throw new IllegalArgumentException("ID " + id + " not found!");
        }
        return new FetchInventoryPacket<>(handlerToSet, storage, pos);
    }

    public static <NS extends NetworkStorage> void handle(final FetchInventoryPacket<NS> message, ServerPlayer player) {
        if (player == null) {
            NetworkClientHandlers.handleReceiveInvData(message.storage, message.pos, message.handler);
        }
        
    }
}
