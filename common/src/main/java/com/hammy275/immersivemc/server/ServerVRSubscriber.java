package com.hammy275.immersivemc.server;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.server.data.LastTickData;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.tracker.ServerTrackerInit;
import com.hammy275.immersivemc.server.tracker.vrhand.AbstractVRHandTracker;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ServerVRSubscriber {

    public static void vrPlayerTick(ServerPlayer player) {
        if (VRPlugin.API.playerInVR(player)) {
            IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);

            double dist = CommonConstants.registerImmersivePickRange;
            Vec3 start = vrPlayer.getHMD().position();
            Vec3 look = vrPlayer.getHMD().getLookAngle();
            Vec3 end = vrPlayer.getHMD().position().add(look.x * dist, look.y * dist, look.z * dist);
            BlockHitResult blockHit = player.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                    player));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                TrackedImmersives.maybeTrackImmersive(player, blockHit.getBlockPos());
            }

            for (AbstractVRHandTracker tracker : ServerTrackerInit.vrPlayerTrackers) {
                tracker.preTick(player);
                if (LastTickVRData.lastTickVRData.get(player.getGameProfile().getName()) != null
                && tracker.isEnabledInConfig(ActiveConfig.getConfigForPlayer(player))) {
                    tracker.tick(player, vrPlayer, LastTickVRData.lastTickVRData.get(player.getGameProfile().getName()));
                }
            }
            LastTickData data = LastTickVRData.lastTickVRData.get(player.getGameProfile().getName());
            Vec3 doubleLast = data == null ? player.position() : data.lastPlayerPos;
            LastTickVRData.lastTickVRData.put(player.getGameProfile().getName(),
                    new LastTickData(vrPlayer, player.position(), doubleLast));
        }
    }

}
