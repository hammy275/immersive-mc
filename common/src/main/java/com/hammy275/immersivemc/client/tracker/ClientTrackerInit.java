package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.common.tracker.AbstractTracker;

import java.util.LinkedList;
import java.util.List;

public class ClientTrackerInit {
    public static final List<AbstractTracker> trackers = new LinkedList<>();

    public static final RangedGrabTrackerClient rangedGrabTracker = new RangedGrabTrackerClient();
    public static final LastVRDataTracker lastVRDataTracker = new LastVRDataTracker();
    public static final ThrowTracker throwTracker = new ThrowTracker();

}
