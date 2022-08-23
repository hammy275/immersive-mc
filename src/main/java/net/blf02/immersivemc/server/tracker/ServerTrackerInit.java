package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.server.tracker.vrhand.AbstractVRHandTracker;
import net.blf02.immersivemc.server.tracker.vrhand.DoorMoveTracker;
import net.blf02.immersivemc.server.tracker.vrhand.HoeTracker;

import java.util.LinkedList;
import java.util.List;

public class ServerTrackerInit {

    public static final List<AbstractTracker> globalTrackers = new LinkedList<>();
    public static final List<AbstractTracker> playerTrackers = new LinkedList<>();
    public static final List<AbstractVRHandTracker> vrPlayerTrackers = new LinkedList<>();

    public static final RangedGrabTrackerServer rangedGrabTracker = new RangedGrabTrackerServer();
    public static final ButtonPushTracker buttonPushTracker = new ButtonPushTracker();
    public static final CampfireTracker campfireTracker = new CampfireTracker();
    public static final LeverTracker leverTracker = new LeverTracker();

    public static final DoorMoveTracker doorTracker = new DoorMoveTracker();
    public static final HoeTracker hoeTracker = new HoeTracker();
}
