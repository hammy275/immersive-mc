package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrackedImmersives {

    public static final List<TrackedImmersiveData<?>> TRACKED_IMMERSIVES = new ArrayList<>();

    public static void tick(MinecraftServer server) {
        // Remove for all logged out players or invalid states (blocks no longer match or player too far away)
        Iterator<TrackedImmersiveData<?>> dataIterator = TRACKED_IMMERSIVES.iterator();
        while (dataIterator.hasNext()) {
            TrackedImmersiveData<?> data = dataIterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(data.playerUUID);
            if (player == null || !data.validForPlayer(player)) {
                if (player != null) {
                    // Called for player == null in ServerSubscriber#onDisconnect().
                    data.getHandler().onStopTracking(player, data.getPos().iterator().next());
                }
                dataIterator.remove();
            }
        }

        // Sync all immersives for all players if inventory contents have changed
        TRACKED_IMMERSIVES.forEach((data) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(data.playerUUID);
            if (data.shouldSync(player)) {
                syncDataToClient(player, data);
            }
        });
    }

    public static void maybeTrackImmersive(ServerPlayer player, BlockPos pos) {
        for (ImmersiveHandler<?> handler : ImmersiveHandlers.HANDLERS) {
            if (!handler.clientAuthoritative() && handler.enabledInConfig(player) && Util.isValidBlocks(handler, pos, player.level())) {
                trackImmersive(player, handler, pos);
                return;
            }
        }
    }

    public static void clearForPlayer(ServerPlayer player) {
        Iterator<TrackedImmersiveData<?>> dataIterator = TRACKED_IMMERSIVES.iterator();
        while (dataIterator.hasNext()) {
            TrackedImmersiveData<?> data = dataIterator.next();
            if (data.playerUUID.equals(player.getUUID())) {
                data.getHandler().onStopTracking(player, data.getPos().iterator().next());
                dataIterator.remove();
            }
        }
    }

    private static void trackImmersive(ServerPlayer player, ImmersiveHandler<?> handler, BlockPos pos) {
        if (TRACKED_IMMERSIVES.stream().anyMatch((data) ->
                Util.getValidBlocks(data.getHandler(), data.getPos().iterator().next(), player.level()).contains(pos) &&
                        data.playerUUID.equals(player.getUUID()))) {
            return;
        }
        TrackedImmersiveData<?> data = new TrackedImmersiveData<>(player.getUUID(), Util.getValidBlocks(handler, pos, player.level()), handler, player.level());
        TRACKED_IMMERSIVES.add(data);
        syncDataToClient(player, data);
    }

    private static void syncDataToClient(ServerPlayer player, TrackedImmersiveData<?> data) {
        if (!data.getHandler().clientAuthoritative()) {
            Network.INSTANCE.sendToPlayer(player, data.getSyncPacket(player));
        }
    }
}
