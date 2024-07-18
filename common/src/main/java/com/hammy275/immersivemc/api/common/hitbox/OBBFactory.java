package com.hammy275.immersivemc.api.common.hitbox;


import com.hammy275.immersivemc.common.api_impl.hitbox.OBBFactoryImpl;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionfc;

/**
 * Factory to create {@link OBB} instances.
 */
public interface OBBFactory {

    /**
     * @return An OBBFactory instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static OBBFactory instance() {
        return OBBFactoryImpl.INSTANCE;
    }

    /**
     * Creates an OBB with no rotation. From a gameplay perspective, this is identical to an AABB. As such, it's
     * recommended to just create an AABB instead.
     * @param aabb The AABB to create the OBB with.
     * @return A new OBB instance.
     */
    public OBB create(AABB aabb);

    /**
     * Creates an OBB with the provided pitch, yaw, and roll rotation. The rotations are applied in the order
     * yaw->pitch->roll.
     * @param aabb The AABB that represents this OBB without rotations.
     * @param pitch The pitch of the rotation.
     * @param yaw The yaw of the rotation.
     * @param roll The roll of the rotation.
     * @return A new OBB instance.
     */
    public OBB create(AABB aabb, double pitch, double yaw, double roll);

    /**
     * Creates an OBB with the provided Quaternion as the rotation.
     * @param aabb The AABB that represents this OBB without rotations.
     * @param rotation The Quaternion specifying how the OBB is rotated.
     * @return A new OBB instance.
     */
    public OBB create(AABB aabb, Quaternionfc rotation);
}
