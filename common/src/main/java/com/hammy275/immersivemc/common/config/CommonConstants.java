package com.hammy275.immersivemc.common.config;

public class CommonConstants {

    public static final int distanceToRemoveImmersive = 12; // Distance to stop tracking immersives from
    public static final int distanceSquaredToRemoveImmersive = distanceToRemoveImmersive * distanceToRemoveImmersive;

    public static final int[] minimumVRAPIVersion = new int[]{3, 0, 0}; // Last index is ignored for being bugfix version

    public static String vrAPIVersionAsString() {
        return minimumVRAPIVersion[0] + "." + minimumVRAPIVersion[1] + "." + minimumVRAPIVersion[2];
    }

    public static String firstNonCompatibleFutureVersionAsString() {
        return (minimumVRAPIVersion[0] + 1) + ".0.0";
    }
}
