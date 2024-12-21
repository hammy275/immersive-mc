package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "blockEntityChanged", at = @At("RETURN"))
    private void immersiveMC$blockEntityChanged(BlockPos blockPos, CallbackInfo ci) {
        // Dirtiness is cleared at the end of each tick, and getting the BlockEntity at the position
        // both introduces performance overhead and can cause unloading BlockEntities to be kept loaded (#498), so
        // we just mark any changes as dirty for us.
        DirtyTracker.markDirty((Level) (Object) this, blockPos);
    }
}
