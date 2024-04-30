package com.hammy275.immersivemc.common.util;

import com.hammy275.immersivemc.common.obb.OBB;
import com.hammy275.immersivemc.common.obb.OBBRotList;
import com.hammy275.immersivemc.common.obb.RotType;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShieldUtil {

    public static OBB getShieldHitbox(Player player, IVRData hand, InteractionHand iHand) {
        // Note: Shield model is about 11/16 (0.6875) blocks tall and 6/16 (3/8) (0.375) blocks wide
        Vec3 pos = getShieldPos(player, hand, iHand);

        float negMult = VRPlugin.API.isLeftHanded(player) ? -1 : 1;
        negMult = iHand == InteractionHand.MAIN_HAND ? negMult * -1 : negMult * 1;

        return new OBB(AABB.ofSize(pos, 0.375, 0.6875, 0.125),
                OBBRotList.create().addRot(Math.toRadians(hand.getYaw()), RotType.YAW)
                        .addRot(Math.toRadians(hand.getPitch()), RotType.PITCH)
                        .addRot(Math.toRadians(hand.getRoll()), RotType.ROLL)
                        .addRot(Math.PI / 2d * negMult, RotType.YAW));
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

}
