package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.tracker.BottleAndBucketTracker;

import java.util.LinkedList;
import java.util.List;

public class ClientTrackerInit {
    public static final List<AbstractTracker> trackers = new LinkedList<>();

    public static final RangedGrabTrackerClient rangedGrabTracker = new RangedGrabTrackerClient();
    public static final ThrowTracker throwTracker = new ThrowTracker();
    public static final FishingReelTracker fishingReelTracker = new FishingReelTracker();
    public static final BottleAndBucketTracker bottleAndBucketTracker = new BottleAndBucketTracker();

    static {
        trackers.add(bottleAndBucketTracker);
    }

}
