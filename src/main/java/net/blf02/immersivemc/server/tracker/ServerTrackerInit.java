package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.tracker.AbstractTracker;

import java.util.LinkedList;
import java.util.List;

public class ServerTrackerInit {

    public static final List<AbstractTracker> globalTrackers = new LinkedList<>();
    public static final List<AbstractTracker> playerTrackers = new LinkedList<>();

    public static final RangedGrabTrackerServer rangedGrabTracker = new RangedGrabTrackerServer();
    public static final ButtonPushTracker buttonPushTracker = new ButtonPushTracker();
}
