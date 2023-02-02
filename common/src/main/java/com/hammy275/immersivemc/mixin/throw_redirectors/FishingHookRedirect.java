package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookRedirect {

    @Inject(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
    at = @At("RETURN"))
    public void atEndOfConstruction(Player player, Level level, int i, int j, CallbackInfo ci) {
        // Sadly, it makes more sense to undo Vanilla's work and handle shooting ourselves here
        if (VRPluginVerify.hasAPI) {
            FishingHook me = (FishingHook) (Object) this;
            // Can use 0 here, since it won't ever be passed to the vanilla value
            // Velocity is guessed through experimentation
            ThrowRedirect.shootFromRotation(me, player, 0, 0, 0, 1f, 0, false);
            ThrowRedirect.deleteRecord(player);
        }
    }
}
