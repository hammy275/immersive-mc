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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LivingEntityMixinProxy {

    private static boolean isImmersiveBlocking(LivingEntity living) {
        if (living instanceof Player player &&
                ActiveConfig.getActiveConfigCommon(player).useShieldImmersive && VRPlugin.API.playerInVR(player)
                && player.getUseItem().isEmpty()) {
            for (InteractionHand iHand : InteractionHand.values()) {
                if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                    return true;
                }
            }
        }
        return false;
    }

    // null if not in VR, Empty ItemStack if not blocking, non-empty itemstack is shield to block with
    @Nullable
    public static ItemStack isDamageSourceBlocked(LivingEntity living, DamageSource damageSource) {
        if (living instanceof Player player &&
                ActiveConfig.getActiveConfigCommon(player).useShieldImmersive && VRPlugin.API.playerInVR(player) && isImmersiveBlocking(player)) {
            // Don't block if on cooldown
            // Guaranteed to not block piercing arrows
            if (damageSource.getDirectEntity() instanceof AbstractArrow arrow) {
                if (arrow.getPierceLevel() > 0) {
                    return ItemStack.EMPTY;
                }
            }

            if (!damageSource.isBypassArmor() && damageSource.getSourcePosition() != null) {
                IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
                for (InteractionHand iHand : InteractionHand.values()) {
                    if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                        // Iterate again if shield is on cooldown
                        if (player.getCooldowns().isOnCooldown(player.getItemInHand(iHand).getItem())) {
                            continue;
                        }
                        // Multiplier based on left handedness and based on which hand we're using
                        float negMult = VRPlugin.API.isLeftHanded(player) ? -1 : 1;
                        negMult = iHand == InteractionHand.MAIN_HAND ? negMult * -1 : negMult * 1;
                        IVRData hand = vrPlayer.getController(iHand.ordinal());
                        float toRot = (float) (Math.PI / 2f * negMult);
                        Vec3 handVec = hand.getLookAngle().yRot(toRot).normalize();
                        Vec3 attackerVec = damageSource.getSourcePosition().vectorTo(player.position()).normalize();
                        double angle = Math.acos(handVec.dot(attackerVec)); // Angle in radians
                        if (angle <= Math.PI && angle >= 2 * Math.PI / 3) { // 60 degrees in each direction from shield vec
                            return player.getItemInHand(iHand);
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        return null;
    }
}
