package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FetchInventoryPacket {

    public final HandlerStorage storage;
    public final ImmersiveHandler handler;
    public final BlockPos pos;

    public FetchInventoryPacket(BlockPos pos) {
        this(null, null, pos);
    }


    public FetchInventoryPacket(@Nullable ImmersiveHandler handler, HandlerStorage storage, BlockPos pos) {
        this.handler = handler;
        this.storage = storage;
        this.pos = pos;
    }

    public boolean hasData() {
        return this.storage != null && this.handler != null;
    }

    public static void encode(FetchInventoryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.hasData());
        if (packet.hasData()) {
            buffer.writeResourceLocation(packet.handler.getID());
            packet.storage.encode(buffer);
        }
    }

    public static FetchInventoryPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ImmersiveHandler handlerToSet = null;
        HandlerStorage storage = null;
        ResourceLocation id;
        if (buffer.readBoolean()) {
            id = buffer.readResourceLocation();
            for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
                if (handler.getID().equals(id)) {
                    handlerToSet = handler;
                    storage = handlerToSet.getEmptyHandler();
                    storage.decode(buffer);
                    break;
                }
            }
            if (storage == null) {
                throw new IllegalArgumentException("ID " + id + " not found!");
            }
        }
        return new FetchInventoryPacket(handlerToSet, storage, pos);
    }

    public static void handle(final FetchInventoryPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) { // Asking for inventory data
                handleServerToClient(player, message.pos);
            } else { // Receiving inventory data
                NetworkClientHandlers.handleReceiveInvData(message.storage, message.pos, message.handler.getID());
            }
        });
        
    }

    public static void handleServerToClient(ServerPlayer player, BlockPos pos) {
        if (NetworkUtil.safeToRun(pos, player)) {
            BlockEntity tileEnt = player.level().getBlockEntity(pos);
            for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
                if (handler.isValidBlock(pos, player.level().getBlockState(pos), tileEnt, player.level())) {
                    HandlerStorage storage = handler.makeInventoryContents(player, pos);
                    Network.INSTANCE.sendToPlayer(player, new FetchInventoryPacket(handler, storage, pos));
                    return;
                }
            }
        }
    }

}
