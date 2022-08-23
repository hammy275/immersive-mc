package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.server.LastTickVRData;
import net.blf02.immersivemc.server.PlayerConfigs;
import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.immersivemc.server.tracker.vrhand.AbstractVRHandTracker;
import net.blf02.vrapi.event.VRPlayerTickEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerVRSubscriber {

    @SubscribeEvent
    public void vrPlayerTick(VRPlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            for (AbstractVRHandTracker tracker : ServerTrackerInit.vrPlayerTrackers) {
                tracker.preTick();
                if (LastTickVRData.lastTickVRData.get(event.player.getGameProfile().getName()) != null
                && tracker.isEnabledInConfig(PlayerConfigs.getConfig(event.player))) {
                    tracker.tick(event.player, event.vrPlayer, LastTickVRData.lastTickVRData.get(event.player.getGameProfile().getName()));
                }
            }
            LastTickData data = LastTickVRData.lastTickVRData.get(event.player.getGameProfile().getName());
            Vec3 doubleLast = data == null ? event.player.position() : data.lastPlayerPos;
            LastTickVRData.lastTickVRData.put(event.player.getGameProfile().getName(),
                    new LastTickData(event.vrPlayer, event.player.position(), doubleLast));
        }
    }

}
