package net.blf02.immersivemc.server;

import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LastTickVRData {
    public static final Map<String, IVRPlayer> lastTickVRData = new HashMap<>();

    public static Vec3 getVelocity(IVRData last, IVRData current) {
        if (last == null) {
            return Vec3.ZERO;
        }
        double x = (current.position().x - last.position().x) / 2d;
        double y = (current.position().y - last.position().y) / 2d;
        double z = (current.position().z - last.position().z) / 2d;
        return new Vec3(x, y, z);
    }
}
