package com.hammy275.immersivemc.common.util;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.common.obb.OBBRotList;
import com.hammy275.immersivemc.common.obb.RotType;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShieldUtil {

    // Amounts below were determined empirically
    private static final double backAmount = 53d/512d;
    private static final double sideAmount = 57d/512d;
    private static final double downAmount = 19d/512d;

    public static OBB getShieldHitbox(Player player, IVRData hand, InteractionHand iHand) {
        // Note: Shield model is about 11/16 (0.6875) blocks tall, 6/16 (3/8) (0.375) blocks in length, and
        // 1/32 blocks wide.
        Vec3 pos = getShieldPos(player, hand, iHand);
        return OBBFactory.instance().create(AABB.ofSize(pos, 0.375 * 1.125, 0.6875 * 1.125, 1d/32d * 6),
                makeRotList(player, hand, iHand).asQuaternion());
    }

    public static Vec3 getShieldPos(Player player, IVRData hand, InteractionHand iHand) {
        // Note that by the right-hand rule:
        // x component is the amount to move left. On the right hand, getNegMult() returns -1, making it move right.
        // y component is the amount to move up. Since it's negative, we move down.
        // z component is the amount to move forward. Since it's negative, we move backwards.
        Vec3 translate = new Vec3(sideAmount * getNegMult(player, iHand), -downAmount, -backAmount)
                .zRot((float) -Math.toRadians(hand.getRoll()))
                .xRot((float) Math.toRadians(hand.getPitch()))
                .yRot((float) -Math.toRadians(hand.getYaw()));

        return hand.position().add(translate);
    }

    private static OBBRotList makeRotList(Player player, IVRData hand, InteractionHand iHand) {
        return OBBRotList.create().addRot(Math.toRadians(hand.getYaw()), RotType.YAW)
                .addRot(Math.toRadians(hand.getPitch()), RotType.PITCH)
                .addRot(Math.toRadians(hand.getRoll()), RotType.ROLL)
                .addRot(Math.PI / 2d * getNegMult(player, iHand), RotType.YAW);
    }

    // Tl;dr: Returns 1 for left hand and -1 for right hand
    private static float getNegMult(Player player, InteractionHand iHand) {
        float negMult = VRPlugin.API.isLeftHanded(player) ? -1 : 1;
        return iHand == InteractionHand.MAIN_HAND ? negMult * -1 : negMult * 1;
    }

}
