package com.hammy275.immersivemc.common.compat.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LootrNullImpl implements LootrCompat {
    @Override
    public @Nullable Container getContainer(ServerPlayer player, BlockPos pos) {
        return null;
    }

    @Override
    public void markOpener(Player player, BlockPos pos) {
        // Intentional noop
    }

    @Override
    public boolean isLootrContainer(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(LootrCompat.BLOCK_TAG);
    }

    @Override
    public boolean openLootrBarrel(BlockPos pos, Player player, boolean nowOpen) {
        return false;
    }

    @Override
    public boolean openLootrShulkerBox(BlockPos pos, Player player, boolean nowOpen) {
        return false;
    }

    @Override
    public boolean isOpen(BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean disableLootrContainerCompat() {
        return true;
    }
}
