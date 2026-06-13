package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class OBBUtil {

    /**
     * Get an OBB that encompasses the provided OBBs. All OBBs must be rotated the same direction
     * @param obbs OBBs to get encompassing OBB of.
     * @return An OBB that encompasses the provided OBBs.
     */
    public static OBB getEncompassingOBB(List<OBB> obbs) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (OBB obb : obbs) {
            AABB underlying = obb.getUnderlyingAABB();
            minX = Math.min(underlying.minX, minX);
            minY = Math.min(underlying.minY, minY);
            minZ = Math.min(underlying.minZ, minZ);
            maxX = Math.max(underlying.maxX, maxX);
            maxY = Math.max(underlying.maxY, maxY);
            maxZ = Math.max(underlying.maxZ, maxZ);
        }

        return OBBFactory.instance().create(new AABB(minX, minY, minZ, maxX, maxY, maxZ), obbs.get(0).getRotation());
    }

}
