package com.hammy275.immersivemc.common.obb;

/**
 * Record containing a rotation for an OBB.
 * @param rot The amount of rotation, in radians.
 * @param rotType The type of rotation (pitch/yaw/roll).
 */
public record OBBRot(float rot, RotType rotType) {

    /**
     * Convenience function to create an OBBRot instance
     * @param rot The amount of rotation, in radians.
     * @param rotType The type of rotation (pitch/yaw/roll).
     * @return A new OBBRot instance.
     */
    public static OBBRot of(double rot, RotType rotType) {
        return new OBBRot((float) rot, rotType);
    }

    /**
     * Convenience function to create an OBBRot instance
     * @param rot The amount of rotation, in radians.
     * @param rotType The type of rotation (pitch/yaw/roll).
     * @return A new OBBRot instance.
     */
    public static OBBRot of(float rot, RotType rotType) {
        return new OBBRot(rot, rotType);
    }
}
