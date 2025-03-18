package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.vr.mixin_proxy.ShieldProxy;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    public void immersiveMC$hurtImmersivelyBlockedShield(float damageAmount, CallbackInfo ci) {
        if (this.useItem.isEmpty() && !ShieldProxy.shieldToDamage.isEmpty()) {
            this.useItem = ShieldProxy.shieldToDamage;
        }
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("RETURN"))
    public void immersiveMC$resetImmersivelyBlockedShieldHurt(float damageAmount, CallbackInfo ci) {
        if (!this.useItem.isEmpty() && !ShieldProxy.shieldToDamage.isEmpty()) {
            this.useItem = ItemStack.EMPTY;
            ShieldProxy.shieldToDamage = ItemStack.EMPTY;
        }
    }

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
}
