package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.gameplay.trackers.BackpackTracker;

@Mixin(value = BackpackTracker.class)
public class BackpackTrackerMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, require = 1)
    public void immersiveMC$notActiveIfUsingBagPreHotswitch(LocalPlayer p, CallbackInfoReturnable<Boolean> cir) {
        if (ActiveConfig.active().reachBehindBagMode.usesOverShoulder()) {
            cir.setReturnValue(false);
        }
    }
}
