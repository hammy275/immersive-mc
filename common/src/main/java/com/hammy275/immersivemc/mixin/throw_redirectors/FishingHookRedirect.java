package com.hammy275.immersivemc.mixin.throw_redirectors;

import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.common.vr.mixin_proxy.ThrowRedirect;
import net.minecraft.server.level.ServerLevel;
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
    public void immersiveMC$atEndOfConstruction(Player player, Level levelIn, int i, int j, CallbackInfo ci) {
        // Sadly, it makes more sense to undo Vanilla's work and handle shooting ourselves here
        if (VRVerify.hasAPI && levelIn instanceof ServerLevel level) {
            FishingHook me = (FishingHook) (Object) this;
            // Velocity is guessed from experimentation
            // Safe to pass the parameters as null, since they're only used in the factory.
            ThrowRedirect.shootFromRotation((ignored, ignored2, ignored3) -> me,
                    level, null, player, 1f);
            ThrowRedirect.deleteRecord(player);
        }
    }
}
