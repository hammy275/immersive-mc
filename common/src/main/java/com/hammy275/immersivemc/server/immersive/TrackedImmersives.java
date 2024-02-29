package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrackedImmersives {

    public static final List<TrackedImmersiveData> TRACKED_IMMERSIVES = new ArrayList<>();
    private static final int trackAllNearbyRange = 6;

    public static void tick(MinecraftServer server) {
        // Remove for all logged out players or invalid states (blocks no longer match or player too far away)
        Iterator<TrackedImmersiveData> dataIterator = TRACKED_IMMERSIVES.iterator();
        while (dataIterator.hasNext()) {
            TrackedImmersiveData data = dataIterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(data.playerUUID);
            if (player == null || !data.validForPlayer(player)) {
                dataIterator.remove();
                DirtyTracker.unmarkDirty(data.getLevel(), data.getPos());
                if (data.getHandler().usesWorldStorage() && player != null) {
                    ImmersiveStorage storage = GetStorage.getStorageIfExists(player, data.getPos());
                    if (storage != null) {
                        storage.returnItems(player);
                        GetStorage.updateStorageOutputAfterItemReturn(player, data.getPos(), storage);
                    }
                }
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
        for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
            if (handler.isValidBlock(pos, player.level())
                && handler.enabledInConfig(ActiveConfig.getConfigForPlayer(player))) {
                trackImmersive(player, handler, pos);
                return;
            }
        }
    }

    private static void trackImmersive(ServerPlayer player, ImmersiveHandler handler, BlockPos pos) {
        for (TrackedImmersiveData data : TRACKED_IMMERSIVES) {
            if (data.getPos().equals(pos)) {
                return;
            }
        }
        TrackedImmersiveData data = new TrackedImmersiveData(player.getUUID(), pos, handler, player.level());
        TRACKED_IMMERSIVES.add(data);
        syncDataToClient(player, data);
    }

    private static void syncDataToClient(ServerPlayer player, TrackedImmersiveData data) {
        Network.INSTANCE.sendToPlayer(player, data.getSyncPacket(player));
        data.didSync(player);
    }
}
