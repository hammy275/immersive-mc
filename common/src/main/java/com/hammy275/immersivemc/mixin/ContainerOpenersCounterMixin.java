package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

    @ModifyVariable(method = "recheckOpeners(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
    at = @At("STORE"), index = 4, ordinal = 0)
    public int openCountI(int originalI, Level level, BlockPos blockPos, BlockState blockState) {
        return originalI + ChestToOpenSet.getOpenCount(blockPos, level);
    }
}
