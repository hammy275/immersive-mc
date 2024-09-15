package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.LivingEntityMixinProxy;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Redirect(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    public boolean isDamageSourceBlocked(LivingEntity shieldHolder, DamageSource damageSource, DamageSource damageSourceAgain, float damage) {
        if (VRPluginVerify.hasAPI) {
            ItemStack stackSource = LivingEntityMixinProxy.isDamageSourceBlocked((LivingEntity) (Object) this,
                    damageSource);
            if (stackSource != null) {
                if (!stackSource.isEmpty() && damage >= 3.0f) {
                    stackSource.hurtAndBreak(1 + Mth.floor(damage), shieldHolder, shieldHolder.getItemInHand(InteractionHand.MAIN_HAND) == stackSource ?
                            EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                return !stackSource.isEmpty();
            }
        }
        return shieldHolder.isDamageSourceBlocked(damageSource);
    }
}
