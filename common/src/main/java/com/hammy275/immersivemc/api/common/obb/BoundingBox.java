package com.hammy275.immersivemc.api.common.obb;

import com.hammy275.immersivemc.common.obb.OBB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Generic interface applied to both OBBs and AABBs, so they both share a common ancestor in the type hierarchy. Other
 * classes may implement this interface in the future.
 */
public interface BoundingBox {

    /**
     * @return This BoundingBox as an OBB if it is one.
     * @throws RuntimeException If this BoundingBox is not an OBB.
     */
    OBB asOBB() throws RuntimeException;

    /**
     * @return This BoundingBox as an AABB if it is one.
     * @throws RuntimeException If this BoundingBox is not an AABB.
     */
    AABB asAABB() throws RuntimeException;

    /**
     * @return Whether this BoundingBox is an OBB.
     */
    default boolean isOBB() {
        return this instanceof OBB;
    }

    /**
     * @return Whether this BoundingBox is an AABB.
     */
    default boolean isAABB() {
        return this instanceof AABB;
    }

    /**
     * Determine if the provided position is inside the provided BoundingBox.
     * @param box The BoundingBox to check if it contains the provided position.
     * @param pos The position to check if inside the provided BoundingBox.
     * @return Whether pos is inside the box.
     */
    public static boolean contains(BoundingBox box, Vec3 pos) {
        return box.isOBB() ? box.asOBB().contains(pos) : box.asAABB().contains(pos);
    }

    /**
     * Get the center of the provided BoundingBox.
     * @param box The BoundingBox to get the center of.
     * @return The center of the provided BoundingBox.
     */
    public static Vec3 getCenter(BoundingBox box) {
        return box.isOBB() ? box.asOBB().getCenter() : box.asAABB().getCenter();
    }
}
