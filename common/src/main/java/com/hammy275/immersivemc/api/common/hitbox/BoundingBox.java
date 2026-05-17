package com.hammy275.immersivemc.api.common.hitbox;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.common.api_impl.hitbox.OBBImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A representation of a box for collision and similar detection purposes. Minecraft's {@link AABB} and ImmersiveMC's
 * {@link OBBImpl} both implement this. Other classes may implement this interface in the future, though only ImmersiveMC
 * should create implementations of this interface.
 * <p>
 * Although this already applies across the API, it's noted here explicitly that implementations of methods are not
 * part of the API contract, only what is specified in the Javadocs and method signatures are. For example, the
 * implementation of {@link #move(BoundingBox, Vec3)} is not guaranteed to make the same calls it does now in the
 * future.
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

    /**
     * Gets the vertices of the provided BoundingBox as an 8-element list.
     * <p>
     * The order of the vertices is guaranteed to match the following criteria:
     * <ul>
     *     <li>The first four elements form a rectangle in a clockwise or counter-clockwise order as would be present
     *         on an outline of the BoundingBox.</li>
     *     <li>The last four elements form a rectangle in a clockwise or counter-clockwise order as would be present
     *         on an outline of the BoundingBox.</li>
     *     <li>For each pair of indices for indexing into the list (0, 4), (1, 5), (2, 6), and (3, 7), each pair
     *         would have a connecting line segment as would be present on an outline of the BoundingBox.</li>
     * </ul>
     * In other words, the list of vertices returned by this method is such that a full outline of the BoundingBox is
     * made by drawing line segments with vertices provided by the following indices into the returned list:
     * (0, 1), (1, 2), (2, 3), (3, 0), (4, 5), (5, 6), (6, 7), (7, 4), (0, 4), (1, 5), (2, 6), (3, 7)
     * @param box The box to get the vertices of.
     * @return The vertices of the provided box as described above.
     */
    public static List<Vec3> vertices(BoundingBox box) {
        return box.isOBB()
                ? box.asOBB().getVertices()
                : ImmersiveLogicHelpers.instance().getVerticesOfAABB(box.asAABB());
    }
}
