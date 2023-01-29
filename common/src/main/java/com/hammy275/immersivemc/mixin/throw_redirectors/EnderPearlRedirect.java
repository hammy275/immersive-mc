package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderpearlItem.class)
public class EnderPearlRedirect {

    @Redirect(method= "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ThrownEnderpearl;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    public void shootFromRotation(ThrownEnderpearl projectile, Entity shooter, float xAngle, float yAngle, float unknown, float velocity, float inaccuracy) {
        if (VRPluginVerify.hasAPI) {
            ThrowRedirect.shootFromRotation(projectile, shooter, xAngle, yAngle, unknown, velocity, inaccuracy);
        } else {
            projectile.shootFromRotation(shooter, xAngle, yAngle, unknown, velocity, inaccuracy);
        }
    }
}
