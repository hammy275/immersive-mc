package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ShieldProxy;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapOperation(method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;acos(D)D"))
    public double immersiveMC$isDamageSourceBlocked(double a, Operation<Double> original, ServerLevel serverLevel, DamageSource damageSource) {
        LivingEntity me = (LivingEntity) (Object) this;
        if (VRPluginVerify.hasAPI) {
            BlocksAttacks blocksAttacks = ShieldProxy.isDamageSourceBlocked(me, damageSource);
            if (blocksAttacks != null) {
                return Math.PI; // Guaranteed to block
            }
        }
        // Since we're forcing Minecraft to assume we're blocking with a shield if we have one, we should
        // block if we're using a blocking item!
        if (me.getUseItem().get(DataComponents.BLOCKS_ATTACKS) != null) {
            original.call(a);
        }
        return 0; // Don't block at all
    }

    @Inject(method = "applyItemBlocking", at = @At("HEAD"))
    private void immersiveMC$isDamageSourceBlockedSetIsBlocking(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Float> cir) {
        ShieldProxy.useIsBlockingMixin = true;
    }

    @Inject(method = "applyItemBlocking", at = @At("RETURN"))
    private void immersiveMC$isDamageSourceBlockedUnsetIsBlocking(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Float> cir) {
        ShieldProxy.useIsBlockingMixin = false;
    }

    @Inject(method = "getItemBlockingWith", at = @At("RETURN"), cancellable = true)
    public void immersiveMC$injectShieldAsItemBlockingWith(CallbackInfoReturnable<ItemStack> cir) {
        if (ShieldProxy.useIsBlockingMixin && cir.getReturnValue() == null && VRPluginVerify.hasAPI) {
            cir.setReturnValue(ShieldProxy.getABlockingShield((LivingEntity) (Object) this));
        }
    }
}
