package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.gameplay.trackers.BackpackTracker;

@Mixin(BackpackTracker.class)
public class BackpackTrackerMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    public void notActiveIfUsingBag(LocalPlayer p, CallbackInfoReturnable<Boolean> cir) {
        if (ActiveConfig.active().reachBehindBagMode.usesOverShoulder()) {
            cir.setReturnValue(false);
        }
    }
}
