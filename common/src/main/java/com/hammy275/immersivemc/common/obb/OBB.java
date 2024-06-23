package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Oriented Bounding Box. This is similar to an Axis-Aligned Bounding Box (AABB), but rotatable. OBBs allow for better
 * accuracy at the cost of a significant performance hit.
 * Internally, the OBB is an AABB, but rotated by some amount on the X, Y, and Z axis. All rotations are in
 * radians.
 */
public class OBB implements BoundingBox {

    private static final double HALFSQRT2 = Math.sqrt(2) / 2d;


    final AABB aabb;
    final Vec3 center;
    final Vector3f centerF;
    final Quaternionf rotation;

    /**
     * Create an OBB from an existing AABB.
     * @param aabb The AABB to create an OBB from.
     */
    public OBB(AABB aabb) {
        this(aabb, new Quaternionf());
    }

    /**
     * Create an OBB from an existing AABB, rotated on the axes in the order yaw, pitch, roll.
     * @param aabb The AABB to create an OBB from.
     * @param pitch The pitch of the OBB, in radians.
     * @param yaw The yaw of the OBB, in radians,
     * @param roll The roll of the OBB, in radians
     */
    public OBB(AABB aabb, double pitch, double yaw, double roll) {
        this(aabb, OBBRotList.create().addRot(yaw, RotType.YAW).addRot(pitch, RotType.PITCH).addRot(roll, RotType.ROLL).asQuaternion());
    }

    /**
     * Create an OBB from an existing AABB, rotated by some arbitrary rotations.
     * @param aabb The AABB to create an OBB from.
     * @param rotation The quaternion representing the rotations applied to this OBB.
     */
    public OBB(AABB aabb, Quaternionf rotation) {
        this.aabb = aabb;
        this.center = aabb.getCenter();
        this.centerF = this.center.toVector3f();
        this.rotation = rotation;
    }

    /**
     * Whether a given point can be found inside this OBB.
     * @param point Point to check.
     * @return Whether the point is in this OBB or not.
     */
    public boolean contains(Vec3 point) {
        // We rotate the start position and the ray direction to be the same as this OBB's, do a normal
        // AABB check, then rotate back to get a proper position.
        Vector3f pt = point.toVector3f();
        pt = pt.sub(this.centerF).rotate(this.rotation).add(this.centerF);
        return this.aabb.contains(toVec3(pt));
    }

    /**
     * Where the provided ray intersects this OBB the soonest.
     * @param rayStart Start of ray
     * @param rayEnd End of ray
     * @return An optional containing where they ray hit this OBB, or an empty Optional if there was no hit.
     */
    public Optional<Vec3> rayHit(Vec3 rayStart, Vec3 rayEnd) {
        // We rotate the start position and the ray direction to be the same as this OBB's, do a normal
        // AABB check, then rotate back to get a proper position.
        Vec3 dir = rayEnd.subtract(rayStart).normalize();
        double dist = rayStart.distanceTo(rayEnd);
        Vector3f dirF = dir.toVector3f().rotate(this.rotation);
        Vector3f rayStartF = rayStart.toVector3f().sub(this.centerF).rotate(this.rotation).add(this.centerF);
        Optional<Vec3> intersect = this.aabb.clip(toVec3(rayStartF), toVec3(rayStartF.add(dirF.mul((float) dist))));
        if (intersect.isPresent()) {
            return Optional.of(toVec3(intersect.get().toVector3f().sub(this.centerF).rotate(this.rotation).add(this.centerF)));
        } else {
            return Optional.empty();
        }
    }

    /**
     * OBBs are rotated AABBs internally. This function retrieves that internal AABB.
     * @return Internal AABB
     */
    public AABB getUnderlyingAABB() {
        return this.aabb;
    }

    /**
     * @return The center of this OBB
     */
    public Vec3 getCenter() {
        return this.center;
    }

    /**
     * @return A reasonably-sized AABB guaranteed to contain this OBB. Bad for collision checks, as it tends to be
     * significantly larger, but good for detecting things that should then be detected on this OBB.
     */
    public AABB getEnclosingAABB() {
        double maxSize = Math.max(Math.max(this.aabb.getXsize(), this.aabb.getYsize()), this.aabb.getZsize());
        return this.aabb.inflate(maxSize * HALFSQRT2);
    }

    public OBB translate(Vec3 translation) {
        return this.translate(translation.x, translation.y, translation.z);
    }

    public OBB translate(Vec3i translation) {
        return this.translate(translation.getX(), translation.getY(), translation.getZ());
    }

    public OBB translate(double x, double y, double z) {
        return new OBB(this.aabb.move(x, y, z));
    }

    @Override
    public OBB asOBB() {
        return this;
    }

    @Override
    public AABB asAABB() {
        throw new RuntimeException("Cannot get AABB as OBB!");
    }

    private Vec3 toVec3(Vector3f vec3f) {
        return new Vec3(vec3f.x(), vec3f.y(), vec3f.z());
    }
}
