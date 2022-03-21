package net.blf02.immersivemc.common.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;

public class Util {

    public static boolean rayTrace(AxisAlignedBB target, Vector3d rayStart, Vector3d rayEnd) {
        // If the start or end of the ray is in the target hitbox, we immediately return true
        if (target.contains(rayStart) || target.contains(rayEnd)) {
            return true;
        }
        // Gets the "hit" for our ray.
        Optional<Vector3d> closestHitOpt = target.clip(rayStart, rayEnd);
        // Return whether or not we have a hit
        return closestHitOpt.isPresent();
    }

    public static Optional<Integer> rayTraceClosest(Vector3d rayStart, Vector3d rayEnd, AxisAlignedBB... targets) {
        double dist = Double.MAX_VALUE;
        Integer winner = null;
        int i = 0;
        for (AxisAlignedBB target : targets) {
            // If the start or end of the ray is in the target hitbox, we immediately return true
            if (target.contains(rayStart)) {
                return Optional.of(i);
            }
            // Gets the "hit" for our ray.
            Optional<Vector3d> closestHitOpt = target.clip(rayStart, rayEnd);
            double distTemp = closestHitOpt.isPresent() ? closestHitOpt.get().distanceTo(rayStart) : -1;
            if (closestHitOpt.isPresent() && distTemp < dist) {
                winner = i;
                dist = distTemp;
            }
            i++;
        }
        return Optional.ofNullable(winner);
    }
}
