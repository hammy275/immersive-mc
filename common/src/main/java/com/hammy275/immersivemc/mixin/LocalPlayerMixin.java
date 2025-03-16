package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void immersiveMC$notCrouchingWhenUseChecking(CallbackInfoReturnable<Boolean> cir) {
        if (ClientMixinProxy.pretendPlayerIsNotCrouching) {
            cir.setReturnValue(false);
        }
    }
}
