package com.hammy275.immersivemc.api.common.hitbox;

import com.hammy275.immersivemc.common.api_impl.hitbox.OBBImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A representation of a box for collision and similar detection purposes. Minecraft's {@link AABB} and ImmersiveMC"s
 * {@link OBBImpl} both implement this. Other classes may implement this interface in the future, though only ImmersiveMC
 * should create implementations of this interface.
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
        return this instanceof OBBImpl;
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

    /**
     * Creates a new BoundingBox which is the same as the provided one, but moved by the provided movement.
     * @param box The original BoundingBox to move.
     * @param movement The amount on each axis to move the BoundingBox by.
     * @return A new BoundingBox, which is the original, but translated by the provided movement.
     */
    public static BoundingBox move(BoundingBox box, Vec3 movement) {
        return box.isOBB() ? box.asOBB().move(movement) : box.asAABB().move(movement);
    }
}
