package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.LivingEntityMixinProxy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Redirect(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    public boolean immersiveMC$isDamageSourceBlocked(LivingEntity shieldHolder, DamageSource damageSource) {
        if (VRPluginVerify.hasAPI) {
            ItemStack stackSource = LivingEntityMixinProxy.isDamageSourceBlocked((LivingEntity) (Object) this,
                    damageSource);
            return stackSource != null && !stackSource.isEmpty();
        }
        return shieldHolder.isDamageSourceBlocked(damageSource);
    }
}
