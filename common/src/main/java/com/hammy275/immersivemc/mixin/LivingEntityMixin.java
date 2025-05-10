package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ShieldProxy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;dot(Lnet/minecraft/world/phys/Vec3;)D"), cancellable = true)
    public void immersiveMC$isDamageSourceBlocked(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity me = (LivingEntity) (Object) this;
        if (VRPluginVerify.hasAPI) {
            boolean doBlock = ShieldProxy.isDamageSourceBlocked(me, damageSource);
            if (doBlock) {
                cir.setReturnValue(true);
                return;
            }
        }
        // Since we're forcing Minecraft to assume we're blocking with a shield if we have one, we should tell it we're
        // not actually blocking if there's no blocking item.
        if (me.getUseItem().getUseAnimation() != UseAnim.BLOCK) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"))
    private void immersiveMC$isDamageSourceBlockedSetIsBlocking(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        ShieldProxy.useIsBlockingMixin = true;
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"))
    private void immersiveMC$isDamageSourceBlockedUnsetIsBlocking(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        ShieldProxy.useIsBlockingMixin = false;
    }

    @Inject(method = "isBlocking", at = @At("RETURN"), cancellable = true)
    public void immersiveMC$injectShieldAsItemBlockingWith(CallbackInfoReturnable<Boolean> cir) {
        if (ShieldProxy.useIsBlockingMixin && VRPluginVerify.hasAPI && !cir.getReturnValue()) {
            cir.setReturnValue(ShieldProxy.getABlockingShield((LivingEntity) (Object) this) != null);
        }
    }

    @Inject(method = "getUsedItemHand", at = @At("HEAD"), cancellable = true)
    public void immersiveMC$injectShieldHandAsUsedItem(CallbackInfoReturnable<InteractionHand> cir) {
        if (!ShieldProxy.shieldToDamage.isEmpty() && ShieldProxy.handWithShield != null) {
            cir.setReturnValue(ShieldProxy.handWithShield);
        }
    }
}
