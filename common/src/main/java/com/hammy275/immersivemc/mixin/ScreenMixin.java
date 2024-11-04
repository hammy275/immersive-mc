package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.immersive_item.ScreenBookImmersive;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class)
public class ScreenMixin {

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void cancelIfRenderingBookScreen(float partialTick, CallbackInfo ci) {
        if (ScreenBookImmersive.renderingFakeScreen) {
            ci.cancel();
        }
    }
}
