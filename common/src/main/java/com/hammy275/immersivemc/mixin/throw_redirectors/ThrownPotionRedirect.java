package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrowablePotionItem.class)
public class ThrownPotionRedirect {

    @Redirect(method= "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    public <T extends Projectile> T immersiveMC$shootFromRotation(Projectile.ProjectileFactory<T> factory,
                                                                  ServerLevel level, ItemStack spawnedFrom,
                                                                  LivingEntity owner, float z, float velocity,
                                                                  float inaccuracy) {
        return ThrowRedirect.throwRedirect(factory, level, spawnedFrom, owner, z, velocity, inaccuracy);
    }
}
