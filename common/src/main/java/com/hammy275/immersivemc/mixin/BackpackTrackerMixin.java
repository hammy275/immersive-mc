package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.gameplay.trackers.BackpackTracker;

// targets is the post-hotswitch version
@Pseudo // Allows compiling even though targets can't be found due to being in hotswitch version
@Mixin(value = BackpackTracker.class, targets = "org.vivecraft.client_vr.gameplay.trackers.BackpackTracker")
public class BackpackTrackerMixin {
    @Inject(method = "isActive(Lnet/minecraft/client/player/LocalPlayer;)Z", at = @At("HEAD"), cancellable = true,
    require = 1)
    public void notActiveIfUsingBagPreHotswitch(LocalPlayer p, CallbackInfoReturnable<Boolean> cir) {
        if (ActiveConfig.ACTIVE.reachBehindBackpackMode.usesOverShoulder()) {
            cir.setReturnValue(false);
        }
    }
}
