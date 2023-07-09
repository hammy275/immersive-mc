package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.server.data.LastTickData;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.server.LastTickVRData;
import com.hammy275.immersivemc.server.PlayerConfigs;
import com.hammy275.immersivemc.server.tracker.vrhand.AbstractVRHandTracker;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ServerVRSubscriber {

    public static void vrPlayerTick(Player player) {
        if (!player.level().isClientSide && VRPlugin.API.playerInVR(player)) {
            IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
            for (AbstractVRHandTracker tracker : ServerTrackerInit.vrPlayerTrackers) {
                tracker.preTick(player);
                if (LastTickVRData.lastTickVRData.get(player.getGameProfile().getName()) != null
                && tracker.isEnabledInConfig(PlayerConfigs.getConfig(player))) {
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
