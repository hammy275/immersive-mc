package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Priority 2000 so Impaled mod's mixin here goes first. They only do a ModifyVariable, so we're good to go after.
@Mixin(value = TridentItem.class, priority = 2000)
public class TridentRedirect {

    @Redirect(method= "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    public <T extends Projectile> T immersiveMC$shootFromRotation(Projectile.ProjectileFactory<T> factory,
                                                                  ServerLevel level, ItemStack spawnedFrom,
                                                                  LivingEntity owner, float z, float velocity,
                                                                  float inaccuracy) {
        return ThrowRedirect.throwRedirect(factory, level, spawnedFrom, owner, z, velocity, inaccuracy);
    }
}
