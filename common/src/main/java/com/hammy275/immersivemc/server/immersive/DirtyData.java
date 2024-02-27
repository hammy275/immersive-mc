package com.hammy275.immersivemc.server.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record DirtyData(BlockPos pos, Level level) {
}
