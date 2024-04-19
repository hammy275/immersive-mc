package com.hammy275.immersivemc.common.obb;

import com.mojang.math.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Oriented Bounding Box. This is similar to an Axis-Aligned Bounding Box (AABB), but rotatable. OBBs allow for better
 * accuracy at the cost of a significant performance hit.
 * Internally, the OBB is an AABB, but rotated by some amount on the X, Y, and Z axis. All rotations are in
 * radians.
 */
public class OBB implements BoundingBox {

    final AABB aabb;
    final Vec3 center;
    final double pitch;
    final double yaw;
    final double roll;
    final Vec3 xLine;
    final Vec3 yLine;
    final Vec3 zLine;

    /**
     * Create an OBB from an existing AABB.
     * @param aabb The AABB to create an OBB from.
     */
    public OBB(AABB aabb) {
        this(aabb, 0, 0, 0);
    }

    /**
     * Create an OBB from an existing AABB, rotated on the axes.
     * @param aabb The AABB to create an OBB from.
     * @param pitch The pitch of the OBB, in radians.
     * @param yaw The yaw of the OBB, in radians,
     * @param roll The roll of the OBB, in radians
     */
    public OBB(AABB aabb, double pitch, double yaw, double roll) {
        this.aabb = aabb;
        this.center = aabb.getCenter();
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;

        // Calculates some things so we don't need to calculate them when position checking
        Quaternionf rot = Axis.ZN.rotation((float) this.roll).rotateX((float) -this.pitch).rotateY((float) -this.yaw);
        Quaternionf rotConj = rot.conjugate(new Quaternionf());
        Vec3 x1 = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 x2 = new Vec3(aabb.maxX, aabb.minY, aabb.minZ);
        Vec3 y1 = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 y2 = new Vec3(aabb.minX, aabb.maxY, aabb.minZ);
        Vec3 z1 = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        Vec3 z2 = new Vec3(aabb.minX, aabb.minY, aabb.maxZ);
        Vec3 x = x2.subtract(x1).normalize();
        Vec3 y = y2.subtract(y1).normalize();
        Vec3 z = z2.subtract(z1).normalize();
        Quaternionf xQuat = new Quaternionf(x.x, x.y, x.z, 0).premul(rot).mul(rotConj);
        Quaternionf yQuat = new Quaternionf(y.x, y.y, y.z, 0).premul(rot).mul(rotConj);
        Quaternionf zQuat = new Quaternionf(z.x, z.y, z.z, 0).premul(rot).mul(rotConj);
        this.xLine = new Vec3(xQuat.x, xQuat.y, xQuat.z);
        this.yLine = new Vec3(yQuat.x, yQuat.y, yQuat.z);
        this.zLine = new Vec3(zQuat.x, zQuat.y, zQuat.z);
    }

    /**
     * Whether a given point can be found inside this OBB.
     * @param point Point to check.
     * @return Whether the point is in this OBB or not.
     */
    @Override
    public boolean contains(Vec3 point) {
        Vec3 centerRay = point.subtract(this.center);

        return Math.abs(centerRay.dot(xLine) * 2) <= (aabb.maxX - aabb.minX) &&
                Math.abs(centerRay.dot(yLine) * 2) <= (aabb.maxY - aabb.minY) &&
                Math.abs(centerRay.dot(zLine) * 2) <= (aabb.maxZ - aabb.minZ);
    }

    /**
     * OBBs are rotated AABBs internally. This function retrieves that internal AABB.
     * @return Internal AABB
     */
    public AABB getUnderlyingAABB() {
        return this.aabb;
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
}
