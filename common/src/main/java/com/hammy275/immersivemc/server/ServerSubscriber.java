package com.hammy275.immersivemc.server;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ServerSubscriber {

    public static void onServerTick(MinecraftServer server) {
        for (AbstractTracker tracker : ServerTrackerInit.globalTrackers) {
            tracker.doTick(null);
        }
        TrackedImmersives.tick(server);
        DirtyTracker.unmarkAllDirty();
    }

    public static void onPlayerTick(Player playerIn) {
        if (playerIn.level().isClientSide || (!VRPluginVerify.clientInVR() && ActiveConfig.FILE.disableOutsideVR)) return;
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
                    new ConfigSyncPacket(ActiveConfig.FILE));
        }
    }

    public static void onPlayerLeave(Player playerIn) {
        if (playerIn instanceof ServerPlayer player) {
            TrackedImmersives.clearForPlayer(player);
            ChestToOpenSet.clearForPlayer(player);
        }
    }
}
