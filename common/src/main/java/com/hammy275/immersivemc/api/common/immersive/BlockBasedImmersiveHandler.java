package com.hammy275.immersivemc.api.common.immersive;

import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * While an {@link BlockBasedImmersive} defines how a client should work
 * with a block-based Immersive, an ImmersiveHandler handles both the server-half of a block-based Immersive, along
 * with the shared, common portion. For example, retrieving what items are stored in a furnace to send to the client
 * (server-specific) and identifying what a furnace is (common) are both handled in an ImmersiveHandler.
 * @param <S>
 */
public non-sealed interface BlockBasedImmersiveHandler<S extends NetworkStorage> extends ImmersiveHandler {

    /**
     * Creates inventory contents on the server to send to clients.
     * @param player Player being sent to.
     * @param pos Position of the block being sent about.
     * @return A NetworkStorage to be sent over the network.
     */
    S makeInventoryContents(ServerPlayer player, BlockPos pos);

    /**
     * @return A new instance of the same type of NetworkStorage as made with {@link #makeInventoryContents}, but in an
     * empty state for purposes such as being decoded into by the client.
     */
    S getEmptyNetworkStorage();

    /**
     * Swaps an item from a player's hand into this immersive (and/or vice-versa).
     *
     * @param slot Slot being swapped with in this immersive.
     * @param hand Player's hand being swapped with.
     * @param pos Position of block being swapped with.
     * @param player Player who is swapping.
     * @param amount An object representing the amount of items to swap. Use
     *               {@link ItemSwapAmount#getNumItemsToSwap()}, passing in the item stack size of the item in the
     *               player's hand to get the amount of items to swap.
     */
    void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount);

    /**
     * Function to determine whether the block has changed its contents to sync to the client since its last sync.
     * Dirtiness is addressed every tick, so for every tick, you should clear ALL flags used for dirtiness.
     * @param player Player to potentially send new data to.
     * @param pos Position of block to check.
     * @return Whether the given block has changed since it was last synced to the client
     */
    boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos);

    /**
     * @param pos Position to check.
     * @param level The level being checked in.
     * @return Whether the supplied block matches what this handler handles.
     */
    boolean isValidBlock(BlockPos pos, Level level);

    /**
     * Function called whenever this immersive is no longer being tracked. Only ever called server-side.
     * @param player Player who is no longer tracking this immersive.
     * @param pos Position of the block no longer being tracked.
     */
    default void onStopTracking(ServerPlayer player, BlockPos pos) {}
}
