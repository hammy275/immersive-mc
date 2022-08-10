package net.blf02.immersivemc.mixin;

import net.blf02.immersivemc.server.ChestToOpenCount;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

    @Inject(method="getOpenCount",
    at=@At("RETURN"), cancellable = true)
    private void onGetOpenCount(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() +
                ChestToOpenCount.chestImmersiveOpenCount.getOrDefault(pos, 0));
    }
}
