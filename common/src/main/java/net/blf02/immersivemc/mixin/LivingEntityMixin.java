package net.blf02.immersivemc.mixin;

import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.common.vr.mixin_proxy.LivingEntityMixinProxy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
    public void isDamageSourceBlocked(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (VRPluginVerify.hasAPI) {
            Boolean damageSourceBlocked = LivingEntityMixinProxy.isDamageSourceBlocked((LivingEntity) (Object) this,
                    damageSource);
            if (damageSourceBlocked != null) {
                cir.setReturnValue(damageSourceBlocked);
            }
        }
    }
}
