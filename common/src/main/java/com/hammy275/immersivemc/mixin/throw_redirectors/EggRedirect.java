package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EggItem.class)
public class EggRedirect {
    @Redirect(method= "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ThrownEgg;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    public void shootFromRotation(ThrownEgg projectile, Entity shooter, float xAngle, float yAngle, float unknown, float velocity, float inaccuracy) {
        if (VRPluginVerify.hasAPI) {
            ThrowRedirect.shootFromRotation(projectile, shooter, xAngle, yAngle, unknown, velocity, inaccuracy);
        } else {
            projectile.shootFromRotation(shooter, xAngle, yAngle, unknown, velocity, inaccuracy);
        }
    }
}
