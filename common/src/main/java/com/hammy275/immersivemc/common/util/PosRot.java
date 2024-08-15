package com.hammy275.immersivemc.common.util;

import net.minecraft.world.phys.Vec3;

/**
 * An object holding both a position and pitch/yaw/roll. Works the same as IVRData.
 * Note that pitch, yaw, and roll should be in degrees for consistency with IVRData.
 */
public record PosRot(Vec3 position, Vec3 lookAngle, float pitch, float yaw, float roll) {

    public Vec3 getLookAngle() {
        return lookAngle;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }
}
