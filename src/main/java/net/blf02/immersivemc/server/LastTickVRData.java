package net.blf02.immersivemc.server;

import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LastTickVRData {
    public static final Map<String, LastTickData> lastTickVRData = new HashMap<>();

    public static Vec3 getVelocity(IVRData last, IVRData current, LastTickData data) {
        // Velocity check here is mainly used for hitting thresholds for hand movements
        // so we account for the player velocity only in such a way where we move towards zero
        if (last == null) {
            return Vec3.ZERO;
        }
        // Use lastPlayerPos and doubleLastPlayerPos since position is a tick ahead of VR data
        Vec3 playerVelocity = Util.getPlayerVelocity(data.doubleLastPlayerPos, data.lastPlayerPos);
        double x = Util.moveTowardsZero(current.position().x - last.position().x, playerVelocity.x);
        double y = Util.moveTowardsZero(current.position().y - last.position().y, playerVelocity.y);
        double z = Util.moveTowardsZero(current.position().z - last.position().z, playerVelocity.z);
        return new Vec3(x, y, z);
    }

    public static double getAllVelocity(IVRData last, IVRData current, LastTickData data) {
        Vec3 vel = getVelocity(last, current, data);
        return Math.abs(vel.x) + Math.abs(vel.y) + Math.abs(vel.z);
    }

}
