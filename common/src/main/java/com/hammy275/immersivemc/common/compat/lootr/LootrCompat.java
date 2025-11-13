package com.hammy275.immersivemc.common.compat.lootr;

import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface LootrCompat {

    public static final TagKey<Block> BLOCK_TAG = TagKey.create(Registries.BLOCK, Util.id("lootr", "containers"));

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

    /**
     * @param pos   Position to check
     * @param level Level to check in
     * @return Whether the block at the given position is a Lootr container.
     */
    public boolean isLootrContainer(BlockPos pos, Level level);

    /**
     * Open/close Lootr barrel if it is one.
     * @param pos The position of the potential barrel.
     * @param player Player that is opening/closing the barrel.
     * @param nowOpen Whether the player is opening or closing the barrel.
     * @return Whether the open/close succeeded. Fails when it isn't a Lootr barrel.
     */
    public boolean openLootrBarrel(BlockPos pos, Player player, boolean nowOpen);

    /**
     * Open/close Lootr Shulker Box if it is one.
     * @param pos The position of the potential Shulker Box.
     * @param player Player that is opening/closing the Shulker Box.
     * @param nowOpen Whether the player is opening or closing the Shulker Box.
     * @return Whether the open/close succeeded. Fails when it isn't a Lootr Shulker Box.
     */
    public boolean openLootrShulkerBox(BlockPos pos, Player player, boolean nowOpen);

    /**
     * @param pos Position to check.
     * @param player Player to check for.
     * @return Whether the supplied Lootr block is open.
     */
    public boolean isOpen(BlockPos pos, Player player);

    /**
     * @return Whether Lootr compat should be fully disabled.
     */
    default boolean disableLootrContainerCompat() {
        return false;
    }

}
