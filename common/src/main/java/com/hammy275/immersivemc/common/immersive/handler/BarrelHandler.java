package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelHandler extends ChestLikeHandler {
    @Override
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
        return blockEntity instanceof BarrelBlockEntity;
    }

    @Override
    public boolean enabledInServerConfig() {
        return ActiveConfig.FILE.useBarrelImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "barrel");
    }
}
