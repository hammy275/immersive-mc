package com.hammy275.immersivemc.common.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface CheckerFunction {
    boolean apply(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level);
}
