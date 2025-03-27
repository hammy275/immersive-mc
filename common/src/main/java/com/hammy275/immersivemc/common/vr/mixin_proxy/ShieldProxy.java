package com.hammy275.immersivemc.common.vr.mixin_proxy;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ShieldProxy {

    public static boolean useIsBlockingMixin = false;

    @Nullable
    public static ItemStack getABlockingShield(LivingEntity living) {
        if (living instanceof Player player && isVRPlayerToManageBySide(player)) {
            for (InteractionHand iHand : InteractionHand.values()) {
                if (player.getItemInHand(iHand).getUseAnimation() == ItemUseAnimation.BLOCK) {
                    return player.getItemInHand(iHand);
                }
            }
        }
        return null;
    }

    @Nullable
    public static BlocksAttacks isDamageSourceBlocked(LivingEntity living, DamageSource damageSource) {
        if (living instanceof Player player && isVRPlayerToManageBySide(player)) {
            IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
            for (InteractionHand iHand : InteractionHand.values()) {
                if (player.getItemInHand(iHand).getUseAnimation() == ItemUseAnimation.BLOCK) {
                    // Iterate again if shield is on cooldown
                    if (player.getCooldowns().isOnCooldown(player.getItemInHand(iHand))) {
                        continue;
                    }
                    // Multiplier based on left-handedness and based on which hand we're using
                    float negMult = VRPlugin.API.isLeftHanded(player) ? -1 : 1;
                    negMult = iHand == InteractionHand.MAIN_HAND ? negMult * -1 : negMult * 1;
                    IVRData hand = vrPlayer.getController(iHand.ordinal());
                    float toRot = (float) (Math.PI / 2f * negMult);
                    Vec3 handVec = hand.getLookAngle().yRot(toRot).normalize();
                    Vec3 attackerVec = damageSource.getSourcePosition().vectorTo(player.position()).multiply(1, 0, 1).normalize();
                    double angle = Math.acos(handVec.dot(attackerVec)); // Angle in radians
                    if (angle <= Math.PI && angle >= 2 * Math.PI / 3) { // 60 degrees in each direction from shield vec
                        ItemStack shieldToDamage = player.getItemInHand(iHand);
                        return shieldToDamage.get(DataComponents.BLOCKS_ATTACKS);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isVRPlayerToManageBySide(Player player) {
        return VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player) && player.getUseItem().isEmpty() &&
                (!player.level().isClientSide || ClientMixinProxy.playerIsLocalPlayer(player)) &&
                ActiveConfig.getActiveConfigCommon(player).useShieldImmersive;
    }
}
