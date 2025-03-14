package com.hammy275.immersivemc.api.common.hitbox;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Optional;

/**
 * Represents an OBB, aka an Oriented Bounding Box. These are best described as a rotatable {@link AABB}. Generally
 * speaking, OBBs are much more accurate due to their ability to rotate, but come at the cost of taking more
 * computational power. ImmersiveMC uses this for things such as the bag's items so that they rotate cleanly
 * with the bag.
 */
public interface OBB extends BoundingBox {

    /**
     * Whether a given point can be found inside this OBB.
     * @param point Point to check.
     * @return Whether the point is in this OBB or not.
     */
    public boolean contains(Vec3 point);

    /**
     * Where the provided ray intersects this OBB the soonest.
     * @param rayStart Start of ray
     * @param rayEnd End of ray
     * @return An optional containing where they ray hit this OBB, or an empty Optional if there was no hit.
     */
    public Optional<Vec3> rayHit(Vec3 rayStart, Vec3 rayEnd);

    /**
     * OBBs are rotated AABBs internally. This function retrieves that internal AABB.
     * @return Internal AABB
     */
    public AABB getUnderlyingAABB();

    /**
     * @return The center of this OBB
     */
    public Vec3 getCenter();

    /**
     * OBBs are rotated AABBs internally. This function retrieves a copy of the {@link Quaternionf} used for rotation.
     * @return A copy of the internal Quaternionf.
     */
    public Quaternionf getRotation();

    /**
     * @return A reasonably-sized AABB guaranteed to contain this OBB. Bad for collision checks, as it tends to be
     * significantly larger than the actual OBB, but good for detecting things that should then be detected on this OBB.
     * Note that the actual sizing of the returned AABB is NOT covered under the API beyond the fact that it is
     * guaranteed to contain the entirety of the OBB within its bounds.
     */
    public AABB getEnclosingAABB();

    /**
     * Creates a new OBB which is this OBB, but moved by some amount.
     * @param movement Movement to move by.
     * @return This OBB, but moved/translated by the provided movement.
     */
    public OBB move(Vec3 movement);
}
