package net.blf02.immersivemc.client.tracker;

import net.blf02.immersivemc.common.tracker.AbstractTracker;

import java.util.LinkedList;
import java.util.List;

public class ClientTrackerInit {
    public static final List<AbstractTracker> trackers = new LinkedList<>();

    public static final RangedGrabTrackerClient rangedGrabTracker = new RangedGrabTrackerClient();

}
