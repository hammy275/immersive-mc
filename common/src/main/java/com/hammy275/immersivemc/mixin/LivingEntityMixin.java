package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ShieldProxy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
        // Since we're forcing Minecraft to assume we're blocking with a shield, we should tell it we're not actually
        // blocking if there's no use item.
        if (me.getUseItem().isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getItemBlockingWith", at = @At("RETURN"), cancellable = true)
    public void immersiveMC$injectShieldAsItemBlockingWith(CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue() == null && VRPluginVerify.hasAPI) {
            cir.setReturnValue(ShieldProxy.getABlockingShield((LivingEntity) (Object) this));
        }
    }
}
