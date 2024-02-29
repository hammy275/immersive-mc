package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public interface ImmersiveHandler {

    /**
     * Creates inventory contents on the server to send to clients.
     * @param player Player being sent to.
     * @param pos Position of the block being sent about.
     * @return A HandlerStorage to be sent over the network.
     */
    HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos);

    /**
     * @return The same type of HandlerStorage as made with makeInventoryContents(), but in an empty state to be
     * decoded into by the client.
     */
    HandlerStorage getEmptyHandler();

    /**
     * Swaps an item from a player's hand into this immersive (and/or vice-versa).
     * @param slot Slot being swapped with in this immersive.
     * @param hand Player's hand being swapped with.
     * @param pos Position of block being swapped with.
     * @param player Player who is swapping.
     * @param mode The placement mode being swapped with (how many items are being swapped).
     */
    void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode);

    /**
     * @param player Player to potentially send new data to.
     * @param pos Position of block to check.
     * @return Whether the given block has changed since it was last synced to the client
     */
    boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos);

    /**
     * When called, the dirtiness checked for in isDirtyForClientSync has been handled. In other words, this function
     * should mark the immersive as not-dirty/no-longer-dirty if needed.
     * @param player Player that no longer has dirty data.
     * @param pos Position of block that is no longer dirty.
     */
    void clearDirtyForClientSync(ServerPlayer player, BlockPos pos);

    /**
     * @return Whether this immersive uses ImmersiveMC's World Storage system.
     */
    boolean usesWorldStorage();

    /**
     * @param pos Position to check.
     * @param level The level being checked in.
     * @return Whether the supplied block matches what this handler handles.
     */
    boolean isValidBlock(BlockPos pos, Level level);

    /**
     * @return Whether the immersive this handler handles is enabled in the supplied config.
     */
    boolean enabledInConfig(ActiveConfig config);

    /**
     * @return A unique ID to identify this handler over the network.
     */
    ResourceLocation getID();
}
