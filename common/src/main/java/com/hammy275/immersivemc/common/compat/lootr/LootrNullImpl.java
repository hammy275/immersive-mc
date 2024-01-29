package com.hammy275.immersivemc.common.compat.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
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
}
