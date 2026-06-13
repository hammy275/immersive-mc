package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * The {@link ImmersiveHandler} for block-based Immersives.
 * @param <S> The type of {@link NetworkStorage} used to sync data from server to client if
 *            {@link #clientAuthoritative()} returns false.
 * @see ImmersiveHandler Information on what ImmersiveHandlers are.
 */
public non-sealed interface BlockBasedImmersiveHandler<S extends NetworkStorage> extends ImmersiveHandler {

    /**
     * Creates inventory contents on the server to send to clients.
     * @param tracker Player being sent to.
     * @param pos Position of the block being sent about.
     * @return A NetworkStorage to be sent over the network.
     */
    S makeInventoryContents(ServerPlayer tracker, BlockPos pos);

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
     * @param tracker Player who is swapping.
     * @param amount An object representing the amount of items to swap. Use
     *               {@link ItemSwapAmount#getNumItemsToSwap()}, passing in the item stack size of the item in the
     *               player's hand to get the amount of items to swap.
     */
    void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount);

    /**
     * Function to determine whether the block has changed its contents to sync to the client since its last sync.
     * Dirtiness is addressed every tick, so for every tick, you should clear ALL flags used for dirtiness.
     * @param tracker Player to potentially send new data to.
     * @param pos Position of block to check.
     * @return Whether the given block has changed since it was last synced to the client
     */
    boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos);

    /**
     * @param pos Position to check.
     * @param level The level being checked in.
     * @return Whether the supplied block matches what this handler handles.
     */
    boolean isValidBlock(BlockPos pos, Level level);

    /**
     * Function called whenever this immersive is no longer being tracked. Only ever called server-side.
     * @param tracker Player who is no longer tracking this immersive.
     * @param pos Position of the block no longer being tracked.
     */
    default void onStopTracking(ServerPlayer tracker, BlockPos pos) {}
}
