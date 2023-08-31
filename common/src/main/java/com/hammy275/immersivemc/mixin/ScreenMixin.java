package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.workaround.ClickHandlerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Low priority to not conflict with default priority, and so we can go first
@Mixin(value = Screen.class, priority = 888)
public class ScreenMixin {

    @Inject(method = "confirmLink(Z)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"),
    cancellable = true)
    public void dontOpenThisScreenAfterURLHandle(boolean bl, CallbackInfo ci) {
        if (((Object) this) instanceof ClickHandlerScreen) {
            Minecraft.getInstance().setScreen(null);
            ci.cancel();
        }
    }
}
