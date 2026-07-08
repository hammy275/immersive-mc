package com.hammy275.immersivemc.server.immersive;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.AfterClientSyncHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.StopTrackPacketWithOwner;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class TrackedImmersives {

    public static final List<TrackedBlockImmersiveData<?>> TRACKED_BLOCK_IMMERSIVES = new ArrayList<>();
    public static final List<TrackedAttachmentImmersiveData<?>> TRACKED_ATTACHMENT_IMMERSIVES = new ArrayList<>();

    public static final Multimap<ServerPlayer, ServerPlayer> PLAYER_TRACKING = HashMultimap.create();

    public static void tick(MinecraftServer server) {
        // Remove for all logged out players or invalid states (blocks no longer match or player too far away)
        Iterator<TrackedBlockImmersiveData<?>> blockDataIterator = TRACKED_BLOCK_IMMERSIVES.iterator();
        while (blockDataIterator.hasNext()) {
            TrackedBlockImmersiveData<?> data = blockDataIterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(data.playerUUID);
            if (player == null || !data.validForPlayer(player)) {
                if (player != null) {
                    // Called for player == null in ServerSubscriber#onDisconnect().
                    data.getHandler().onStopTracking(player, data.getPos().iterator().next());
                }
                blockDataIterator.remove();
            }
        }
        Iterator<TrackedAttachmentImmersiveData<?>> attachmentDataIterator = TRACKED_ATTACHMENT_IMMERSIVES.iterator();
        List<Pair<TrackedAttachmentImmersiveData<?>, ServerPlayer>> maybeTrackPairs = new ArrayList<>();
        while (attachmentDataIterator.hasNext()) {
            TrackedAttachmentImmersiveData<?> data = attachmentDataIterator.next();
            if (!data.stillValid()) {
                data.handler().onStopTracking(data.tracker(), data.owner());
                attachmentDataIterator.remove();
            } else if (data.owner() == data.tracker()) {
                ServerLevel level = data.owner().level();
                level.getPlayers(potentialTracker -> potentialTracker != data.owner() &&
                        potentialTracker.distanceToSqr(data.owner()) < CommonConstants.distanceSquaredToRemoveAttachmentImmersive)
                        .forEach(potentialTracker -> maybeTrackPairs.add(new Pair<>(data, potentialTracker)));
            }
        }
        maybeTrackPairs.forEach(pair ->
                maybeTrackImmersive(pair.getSecond(), pair.getFirst().owner(), pair.getFirst().handler()));

        // Sync all immersives for all players if inventory contents have changed
        TRACKED_BLOCK_IMMERSIVES.forEach((data) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(data.playerUUID);
            if (data.shouldSync(player)) {
                syncDataToClient(player, data);
            }
        });
        TRACKED_ATTACHMENT_IMMERSIVES.forEach(data -> {
            if (data.shouldSync()) {
                syncDataToClient(data);
            }
        });
    }

    public static void maybeTrackImmersive(ServerPlayer player, BlockPos pos) {
        for (BlockBasedImmersiveHandler<?> handler : ImmersiveHandlers.BLOCK_HANDLERS) {
            if (!handler.clientAuthoritative() && handler.enabledInConfig(player) && Util.isValidBlocks(handler, pos, player.level())) {
                trackImmersive(player, handler, pos);
                return;
            }
        }
    }

    public static void maybeTrackImmersive(ServerPlayer tracker, ServerPlayer owner, PlayerAttachmentImmersiveHandler<?> handler) {
        Optional<TrackedAttachmentImmersiveData<?>> existingData = getTrackedData(tracker, owner, handler);
        if (existingData.isEmpty() && VRVerify.playerInVR(owner) &&
                (tracker == owner || PLAYER_TRACKING.containsEntry(tracker, owner))) {
            TrackedAttachmentImmersiveData<?> data = new TrackedAttachmentImmersiveData<>(tracker, owner, handler);
            TRACKED_ATTACHMENT_IMMERSIVES.add(data);
            syncDataToClient(data);
        }
    }

    public static void stopTracking(PlayerAttachmentImmersiveHandler<?> handler, ServerPlayer owner) {
        Iterator<TrackedAttachmentImmersiveData<?>> iterator = TRACKED_ATTACHMENT_IMMERSIVES.iterator();
        while (iterator.hasNext()) {
            TrackedAttachmentImmersiveData<?> data = iterator.next();
            if (data.handler() == handler && data.owner() == owner) {
                Network.INSTANCE.sendToPlayer(data.tracker(), new StopTrackPacketWithOwner(handler.getID(), owner.getUUID()));
                iterator.remove();
            }
        }
    }

    public static void clearForPlayer(ServerPlayer player) {
        Iterator<TrackedBlockImmersiveData<?>> blockDataIterator = TRACKED_BLOCK_IMMERSIVES.iterator();
        while (blockDataIterator.hasNext()) {
            TrackedBlockImmersiveData<?> data = blockDataIterator.next();
            if (data.playerUUID.equals(player.getUUID())) {
                data.getHandler().onStopTracking(player, data.getPos().iterator().next());
                blockDataIterator.remove();
            }
        }
        Iterator<TrackedAttachmentImmersiveData<?>> attachmentDataIterator = TRACKED_ATTACHMENT_IMMERSIVES.iterator();
        while (attachmentDataIterator.hasNext()) {
            TrackedAttachmentImmersiveData<?> data = attachmentDataIterator.next();
            if (data.tracker() == player) {
                data.handler().onStopTracking(data.tracker(), data.owner());
                attachmentDataIterator.remove();
            }
        }
    }

    public static void onDisconnect(ServerPlayer player) {
        clearForPlayer(player);
        // Only clear PLAYER_TRACKING on disconnect since clearForPlayer() is also called when closing the config menu
        PLAYER_TRACKING.removeAll(player);
    }

    public static List<ServerPlayer> getPlayersTrackingPos(MinecraftServer server, Level level, BlockPos pos) {
        return TRACKED_BLOCK_IMMERSIVES.stream()
                .filter(data -> data.getLevel() == level && data.getPos().contains(pos))
                .map(data -> data.playerUUID)
                .distinct()
                .map(uuid -> server.getPlayerList().getPlayer(uuid))
                .toList();
    }

    public static Optional<TrackedAttachmentImmersiveData<?>> getTrackedData(ServerPlayer tracker, ServerPlayer owner, PlayerAttachmentImmersiveHandler<?> handler) {
        return TRACKED_ATTACHMENT_IMMERSIVES.stream()
                .filter(data -> tracker == data.tracker() && owner == data.owner())
                .findAny();
    }

    private static void trackImmersive(ServerPlayer player, BlockBasedImmersiveHandler<?> handler, BlockPos pos) {
        if (TRACKED_BLOCK_IMMERSIVES.stream().anyMatch((data) ->
                Util.getValidBlocks(data.getHandler(), data.getPos().iterator().next(), player.level()).contains(pos) &&
                        data.playerUUID.equals(player.getUUID()))) {
            return;
        }
        TrackedBlockImmersiveData<?> data = new TrackedBlockImmersiveData<>(player.getUUID(), Util.getValidBlocks(handler, pos, player.level()), handler, player.level());
        TRACKED_BLOCK_IMMERSIVES.add(data);
        syncDataToClient(player, data);
    }

    private static void syncDataToClient(ServerPlayer player, TrackedBlockImmersiveData<?> data) {
        if (!data.getHandler().clientAuthoritative()) {
            Network.INSTANCE.sendToPlayer(player, data.getSyncPacket(player));
            if (data.getHandler() instanceof AfterClientSyncHandler afterHandler) {
                afterHandler.afterClientSync(player, data.getPos());
            }
        }
    }

    private static void syncDataToClient(TrackedAttachmentImmersiveData<?> data) {
        if (!data.handler().clientAuthoritative()) {
            Network.INSTANCE.sendToPlayer(data.tracker(), data.getSyncPacket());
        }
    }
}
