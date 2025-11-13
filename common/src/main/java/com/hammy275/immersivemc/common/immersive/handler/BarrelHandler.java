package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;

public class BarrelHandler extends ChestLikeHandler {
    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof BarrelBlock && super.isValidBlock(pos, level);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useBarrelImmersive;
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        super.onStopTracking(player, pos);
        if (ChestToOpenSet.getOpenCount(pos, player.level()) == 0 && player.level().getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.recheckOpen();
        }
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("barrel");
    }
}
