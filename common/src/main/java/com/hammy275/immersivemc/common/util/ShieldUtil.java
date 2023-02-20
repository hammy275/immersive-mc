package com.hammy275.immersivemc.common.util;

import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShieldUtil {

    public static AABB getShieldHitbox(Player player, IVRData hand, InteractionHand iHand) {
        // Note: Shield model is about 11/16 (0.6875) blocks tall and 6/16 (3/8) (0.375) blocks wide
        Vec3 shieldDir = hand.getLookAngle();
        Vec3 pos = getShieldPos(player, hand, iHand);

        // Add 0.125 to all coordinates so shield hitbox actually has some size to it
        AABB vertical = AABB.ofSize(pos, 0.375*Math.abs(shieldDir.x) + 0.125,
                0.6875,
                0.375*Math.abs(shieldDir.z) + 0.125);
        AABB horizontal = AABB.ofSize(pos, 0.6875 * (1 - Math.abs(shieldDir.x)) + 0.375 * Math.abs(shieldDir.x) + 0.125,
                0.1875, // 3/16 (0.125 + 1/16 thickness)
                0.6875 * (1 - Math.abs(shieldDir.z)) + 0.375 * Math.abs(shieldDir.z));


        // horizMod = rollMod
        double horizMod = Math.abs(Math.sin(hand.getRoll() * Math.PI / 180));
        double vertMod = 1 - horizMod;
        // growAmount before the * is 0.5 at max. * 2 for making it 0-1, then / 2 for .inflate doubling up.
        double growAmount = (horizMod < 0.5 ? horizMod : 1 - horizMod) * 0.5;

        /* Effectively, we create a 0 degree and 90-degree rotation hitbox, and average them based on our current
           rotation. */
        return new AABB(avg(vertical.minX, horizontal.minX, vertMod, horizMod),
                    avg(vertical.minY, horizontal.minY, vertMod, horizMod),
                    avg(vertical.minZ, horizontal.minZ, vertMod, horizMod),
                    avg(vertical.maxX, horizontal.maxX, vertMod, horizMod),
                    avg(vertical.maxY, horizontal.maxY, vertMod, horizMod),
                    avg(vertical.maxZ, horizontal.maxZ, vertMod, horizMod))
                // And grow it a bit, as the closer to 45 degrees we are, the taller it needs to grow
                .inflate(0, growAmount, 0);
    }

    public static Vec3 getShieldPos(Player player, IVRData hand, InteractionHand iHand) {
        Vec3 shieldDir = hand.getLookAngle();
        float negMult = VRPlugin.API.isLeftHanded(player) ? -1 : 1;
        negMult = iHand == InteractionHand.MAIN_HAND ? negMult * -1 : negMult * 1;
        Vec3 forward = hand.getLookAngle()
                .yRot((float) (Math.PI / 2f * negMult))
                .normalize();
        return hand.position().add(forward.multiply(0.125, 0.125, 0.125)) // Move hitbox forward to be with shield
                .add(shieldDir.multiply(-0.0625, -0.0625, -0.0625)); // And back a bit on the other axis to
                                                                     // match where shield actually is on arm
    }

    private static double avg(double vertical, double horizontal, double verticalMod, double horizontalMod) {
        // Weighted average vertical with verticalMod and horizontal with horizontalMod
        return vertical * verticalMod + horizontal * horizontalMod;
    }

}
