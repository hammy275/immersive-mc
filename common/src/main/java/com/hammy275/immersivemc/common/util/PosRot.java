package com.hammy275.immersivemc.common.util;

import net.minecraft.world.phys.Vec3;

/**
 * An object holding both a position and pitch/yaw/roll. Works the same as IVRData.
 * Note that pitch, yaw, and roll should be in degrees for consistency with IVRData.
 */
public record PosRot(Vec3 position, Vec3 lookAngle, double pitch, double yaw, double roll) {

    public Vec3 getPos() {
        return position;
    }

    public Vec3 getDir() {
        return lookAngle;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public float getPitchF() {
        return (float) pitch;
    }

    public float getYawF() {
        return (float) yaw;
    }

    public float getRollF() {
        return (float) roll;
    }
}
