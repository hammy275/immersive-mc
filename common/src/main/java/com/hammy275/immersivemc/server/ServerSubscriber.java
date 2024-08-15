package com.hammy275.immersivemc.server;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BookData;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCLevelStorage;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ServerSubscriber {

    public static MinecraftServer server;

    public static void onServerTick(MinecraftServer server) {
        ServerSubscriber.server = server; // Not a fan of this, but no better way to hold onto the server instance.
        ImmersiveMCLevelStorage.getAllBookDatas(server).forEach(BookData::lecternServerTick);
        for (AbstractTracker tracker : ServerTrackerInit.globalTrackers) {
            tracker.doTick(null);
        }
        TrackedImmersives.tick(server);
        DirtyTracker.unmarkAllDirty(); // Remove dirtiness for block entities
        ImmersiveMCLevelStorage.unmarkAllItemStoragesDirty(server);
        ImmersiveMCLevelStorage.getAllBookDatas(server).forEach(BookData::markNoLongerDirty);
    }

    public static void onPlayerTick(Player playerIn) {
        if (playerIn.level.isClientSide || (!VRPluginVerify.playerInVR((ServerPlayer) playerIn) && ActiveConfig.FILE.disableOutsideVR)) return;
        ServerPlayer player = (ServerPlayer) playerIn;
        for (AbstractTracker tracker : ServerTrackerInit.playerTrackers) {
            tracker.doTick(player);
        }
        if (VRPluginVerify.hasAPI) {
            ServerVRSubscriber.vrPlayerTick(player);
        }

        // Get looking at immersive
        HitResult hit = player.pick(CommonConstants.registerImmersivePickRange, 0, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() != HitResult.Type.MISS) {
            TrackedImmersives.maybeTrackImmersive(player, blockHit.getBlockPos());
        }
    }

    public static void onPlayerJoin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Network.INSTANCE.sendToPlayer(serverPlayer,
                    new ConfigSyncPacket(ActiveConfig.FILE,
                            ImmersiveHandlers.HANDLERS.stream()
                                    .filter((handler) -> !handler.clientAuthoritative())
                                    .map(ImmersiveHandler::getID)
                                    .toList()
                    ));
        }
    }

    public static void onPlayerLeave(Player playerIn) {
        if (playerIn instanceof ServerPlayer player) {
            TrackedImmersives.clearForPlayer(player);
            ChestToOpenSet.clearForPlayer(player);
        }
    }
}
