package com.hammy275.immersivemc.forge.mixin;

import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelForge118Mixin {

    // No remapping, Forge-exclusive method
    @Inject(method = "blockEntityChangedWithoutNeighborUpdates", at = @At("RETURN"), remap = false)
    private void immersiveMC$blockEntityChangedWithoutNeighborUpdates(BlockPos blockPos, CallbackInfo ci) {
        DirtyTracker.markDirty((Level) (Object) this, blockPos);
    }
}
