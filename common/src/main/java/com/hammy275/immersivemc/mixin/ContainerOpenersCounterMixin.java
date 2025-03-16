package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {
    @Inject(method = "getOpenCount", at = @At("RETURN"), cancellable = true)
    private void immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + ChestToOpenSet.getOpenCount(pos, level));
    }
}
