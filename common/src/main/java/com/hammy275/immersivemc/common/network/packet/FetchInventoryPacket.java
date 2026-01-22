package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FetchInventoryPacket<S extends NetworkStorage> {

    public final S storage;
    public final ImmersiveHandler<S> handler;
    public final BlockPos pos;

    public FetchInventoryPacket(ImmersiveHandler<S> handler, S storage, BlockPos pos) {
        this.handler = handler;
        this.storage = storage;
        this.pos = pos;
    }

    public static <NS extends NetworkStorage> void encode(FetchInventoryPacket<NS> packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.handler.getID());
        packet.storage.encode(buffer);
    }

    @SuppressWarnings("unchecked")
    public static <NS extends NetworkStorage> FetchInventoryPacket<NS> decode(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ImmersiveHandler<NS> handlerToSet = null;
        NS storage = null;
        ResourceLocation id = buffer.readResourceLocation();
        for (ImmersiveHandler<?> handler : ImmersiveHandlers.HANDLERS) {
            if (handler.getID().equals(id)) {
                handlerToSet = (ImmersiveHandler<NS>) handler;
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
