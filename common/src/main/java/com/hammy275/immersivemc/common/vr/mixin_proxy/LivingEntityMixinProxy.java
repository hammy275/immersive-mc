package com.hammy275.immersivemc.common.vr.mixin_proxy;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

public class LivingEntityMixinProxy {

    private static boolean isImmersiveBlocking(LivingEntity living) {
        if (living instanceof Player player &&
                ActiveConfig.immersiveShield && VRPlugin.API.playerInVR(player)
                && player.getUseItem().isEmpty()) {
            for (InteractionHand iHand : InteractionHand.values()) {
                if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean isDamageSourceBlocked(LivingEntity living, DamageSource damageSource) {
        if (living instanceof Player player &&
                ActiveConfig.immersiveShield && VRPlugin.API.playerInVR(player) && isImmersiveBlocking(player)) {
            // Guaranteed to not block piercing arrows
            if (damageSource.getDirectEntity() instanceof AbstractArrow arrow) {
                if (arrow.getPierceLevel() > 0) {
                    return false;
                }
            }

            if (!damageSource.isBypassArmor() && damageSource.getSourcePosition() != null) {
                IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
                for (InteractionHand iHand : InteractionHand.values()) {
                    if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                        IVRData hand = vrPlayer.getController(iHand.ordinal());
                        float toRot = (float) (iHand == InteractionHand.MAIN_HAND ? Math.PI / -2f : Math.PI / 2f);
                        Vec3 handVec = hand.getLookAngle().yRot(toRot).normalize();
                        Vec3 attackerVec = damageSource.getSourcePosition().vectorTo(player.position()).normalize();
                        double angle = Math.acos(handVec.dot(attackerVec)); // Angle in radians
                        if (angle <= Math.PI && angle >= 2 * Math.PI / 3) { // 60 degrees in each direction from shield vec
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return null;
    }
}
