package com.hammy275.immersivemc.common.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface ImmersiveChecker {
    boolean apply(BlockPos pos, Level level);
}
