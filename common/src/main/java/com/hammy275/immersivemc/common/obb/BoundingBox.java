package com.hammy275.immersivemc.common.obb;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Generic interface applied to both OBB's and AABB's, so they can be stored in the same place. Also contains some
 * functions that can be shared between both to ease using the two.
 */
public interface BoundingBox {

    OBB asOBB();

    AABB asAABB();

    default boolean isOBB() {
        return this instanceof OBB;
    }

    default boolean isAABB() {
        return this instanceof AABB;
    }

    public boolean contains(Vec3 vec3);
}
