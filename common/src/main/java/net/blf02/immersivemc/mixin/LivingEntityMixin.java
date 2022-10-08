package net.blf02.immersivemc.mixin;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this) instanceof Player player &&
                ActiveConfig.immersiveShield && VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)) {
            for (InteractionHand iHand : InteractionHand.values()) {
                if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    public void isDamageSourceBlocked(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this) instanceof Player player &&
                ActiveConfig.immersiveShield && VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)) {
            if (damageSource.getDirectEntity() instanceof AbstractArrow arrow) {
                if (arrow.getPierceLevel() > 0) {
                    cir.setReturnValue(false);
                }
            }

            if (!damageSource.isBypassArmor() && player.isBlocking() && damageSource.getSourcePosition() != null) {
                IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
                for (InteractionHand iHand : InteractionHand.values()) {
                    if (player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                        IVRData hand = vrPlayer.getController(iHand.ordinal());
                        float toRot = (float) (iHand == InteractionHand.MAIN_HAND ? Math.PI / -2f : Math.PI / 2f);
                        Vec3 handVec = hand.getLookAngle().yRot(toRot).normalize();
                        Vec3 attackerVec = damageSource.getSourcePosition().vectorTo(player.position()).normalize();
                        if (handVec.dot(attackerVec) < 0) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
            cir.setReturnValue(false);
        }
    }
}
