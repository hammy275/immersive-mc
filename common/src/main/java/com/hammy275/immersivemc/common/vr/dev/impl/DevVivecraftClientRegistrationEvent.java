package com.hammy275.immersivemc.common.vr.dev.impl;

import com.hammy275.immersivemc.common.vr.dev.DevVRState;
import org.apache.commons.lang3.NotImplementedException;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.event.VivecraftClientRegistrationEvent;

import java.util.List;

public class DevVivecraftClientRegistrationEvent implements VivecraftClientRegistrationEvent {

    @Override
    public void registerTrackers(Tracker... trackers) {
        for (Tracker tracker : trackers) {
            if (tracker.processType() == Tracker.ProcessType.PER_FRAME) {
                throw new NotImplementedException("PER_FRAME not implemented");
            }
        }
        DevVRState.trackers.addAll(List.of(trackers));
    }

    @Override
    public void registerInteractModules(InteractModule... interactModules) {
        DevVRState.interactModules.addAll(List.of(interactModules));
    }
}
