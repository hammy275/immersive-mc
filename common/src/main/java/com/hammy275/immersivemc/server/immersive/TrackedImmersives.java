package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class TrackedImmersives {

    public static final List<TrackedImmersiveData> TRACKED_IMMERSIVES = new ArrayList<>();

    public static void tick(MinecraftServer server) {
        // Remove for all logged out players or invalid states (blocks no longer match or player too far away)
        TRACKED_IMMERSIVES.removeIf((data) -> server.getPlayerList().getPlayer(data.playerUUID) == null ||
                                    !data.validForPlayer(server.getPlayerList().getPlayer(data.playerUUID)));

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
            if (handler.isValidBlock(pos, player.level().getBlockState(pos), player.level().getBlockEntity(pos), player.level())) {
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
