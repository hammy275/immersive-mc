package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.tracker.AbstractTracker;

import java.util.LinkedList;
import java.util.List;

public class ServerTrackerInit {

    public static final List<AbstractTracker> trackers = new LinkedList<>();

    public static final RangedGrabTrackerServer rangedGrabTracker = new RangedGrabTrackerServer();
}
