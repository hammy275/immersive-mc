package net.blf02.immersivemc.common.util;

import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShieldUtil {

    public static AABB getShieldHitbox(IVRData hand, InteractionHand iHand) {
        // This function doesn't account for changes in the y size. Maybe it will someday...
        // (probably need some trig for that lol)

        // Note: Shield model is about 11/16 (0.6875) blocks tall and 6/16 (3/8) (0.375) blocks wide
        Vec3 shieldDir = hand.getLookAngle();
        Vec3 pos = getShieldPos(hand, iHand);
        // Add 0.125 to all coordinates so shield hitbox actually has some size to it
        return AABB.ofSize(pos, 0.375*Math.abs(shieldDir.x) + 0.125,
                0.6875,
                0.375*Math.abs(shieldDir.z) + 0.125);
    }

    public static Vec3 getShieldPos(IVRData hand, InteractionHand iHand) {
        Vec3 shieldDir = hand.getLookAngle();
        Vec3 forward = hand.getLookAngle()
                .yRot((float) (iHand == InteractionHand.MAIN_HAND ? Math.PI / -2f : Math.PI / 2f))
                .normalize();
        return hand.position().add(forward.multiply(0.125, 0.125, 0.125)) // Move hitbox forward to be with shield
                .add(shieldDir.multiply(-0.0625, -0.0625, -0.0625)); // And back a bit on the other axis to
                                                                     // match where shield actually is on arm
    }

}
