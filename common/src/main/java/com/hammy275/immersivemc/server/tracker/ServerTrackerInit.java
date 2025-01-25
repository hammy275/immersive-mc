package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.tracker.BottleAndBucketTracker;
import com.hammy275.immersivemc.server.tracker.vrhand.*;

import java.util.LinkedList;
import java.util.List;

public class ServerTrackerInit {

    public static final List<AbstractTracker> globalTrackers = new LinkedList<>();
    public static final List<AbstractTracker> playerTrackers = new LinkedList<>();
    public static final List<AbstractVRHandTracker> vrPlayerTrackers = new LinkedList<>();

    public static final RangedGrabTrackerServer rangedGrabTracker = new RangedGrabTrackerServer();
    public static final ButtonPushTracker buttonPushTracker = new ButtonPushTracker();
    public static final CampfireTracker campfireTracker = new CampfireTracker();

    public static final PetTracker petTracker = new PetTracker();
    public static final ArmorTracker armorTracker = new ArmorTracker();
    public static final FeedAnimalsTracker feedAnimalsTracker = new FeedAnimalsTracker();
    public static final CauldronTracker cauldronTracker = new CauldronTracker();
    public static final BottleAndBucketTracker bottleAndBucketTracker = new BottleAndBucketTracker();

    static {
        playerTrackers.add(bottleAndBucketTracker);
    }
}
