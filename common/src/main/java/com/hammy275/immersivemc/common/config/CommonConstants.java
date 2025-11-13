package com.hammy275.immersivemc.common.config;

public class CommonConstants {

    // ImmersiveMC API Versioning
    // 1.0: 1.5.0 Beta 3
    // 2.0: 1.5.0
    // 3.0: 1.6.0 Alpha 1
    public static final int API_MAJOR_VERSION = 3;
    public static final int API_MINOR_VERSION = 0;

    public static final int distanceToRemoveImmersive = 16; // Distance to stop tracking immersives from
    public static final int distanceSquaredToRemoveImmersive = distanceToRemoveImmersive * distanceToRemoveImmersive;
    public static final double registerImmersivePickRange = distanceToRemoveImmersive - 1;

    public static final int[] minimumVRAPIVersion = new int[]{3, 0, 3};

    // Vibration values
    public static final float vibrationTimePlayerActionAlert = 0.05f; // Such as opening the bag from reach behind
    public static final float vibrationTimeWorldInteraction = 0.15f; // Such as opening/closing a door

    public static final float vibrationTimeRangedGrab = vibrationTimePlayerActionAlert / 2f;

    public static String vrAPIVersionAsString() {
        return minimumVRAPIVersion[0] + "." + minimumVRAPIVersion[1] + "." + minimumVRAPIVersion[2];
    }

    public static String firstNonCompatibleFutureVersionAsString() {
        return (minimumVRAPIVersion[0] + 1) + ".0.0";
    }

}
