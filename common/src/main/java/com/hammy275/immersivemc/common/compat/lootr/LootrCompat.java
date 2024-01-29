package com.hammy275.immersivemc.common.compat.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface LootrCompat {

    /**
     * Gets the container Lootr-handled block, or returns null if the target isn't a Lootr-handled block.
     * @param player Player to get contents for.
     * @param pos Position of block.
     * @return A list of items in the block, or null if there isn't one.
     */
    @Nullable
    public Container getContainer(ServerPlayer player, BlockPos pos);

    /**
     * Mark the specified player as having opened this Lootr-handled block, if this is a Lootr-handled block.
     *
     * @param player Player to mark
     * @param pos    Position of chest
     */
    public void markOpener(Player player, BlockPos pos);
}
