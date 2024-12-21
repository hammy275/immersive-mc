package com.hammy275.immersivemc.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface ProjectileAccessor {

    @Invoker("onHit")
    public void immersiveMC$onHitRes(HitResult hitResult);
}
