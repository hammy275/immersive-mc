package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.server.LastTickVRData;
import net.blf02.vrapi.event.VRPlayerTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerVRSubscriber {

    @SubscribeEvent
    public void vrPlayerTick(VRPlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            for (AbstractVRHandTracker tracker : ServerTrackerInit.vrPlayerTrackers) {
                tracker.preTick();
                if (LastTickVRData.lastTickVRData.get(event.player.getGameProfile().getName()) != null) {
                    tracker.tick(event.player, event.vrPlayer, LastTickVRData.lastTickVRData.get(event.player.getGameProfile().getName()));
                }
                LastTickVRData.lastTickVRData.put(event.player.getGameProfile().getName(), event.vrPlayer);
            }
        }
    }

}
