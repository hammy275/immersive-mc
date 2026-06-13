package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * The {@link ImmersiveHandler} for player-attachment Immersives.
 * @param <S> The type of {@link NetworkStorage} used to sync data from server to client if
 *            {@link #clientAuthoritative()} returns false.
 * @see ImmersiveHandler Information on what ImmersiveHandlers are.
 */
public non-sealed interface PlayerAttachmentImmersiveHandler<S extends NetworkStorage> extends ImmersiveHandler {

    /**
     * Creates inventory contents on the server to send to clients.
     *
     * @param tracker Player who's receiving the inventory contents.
     * @param owner Player whose inventory contents are being made.
     * @return A NetworkStorage to be sent over the network.
     */
    S makeInventoryContents(ServerPlayer tracker, ServerPlayer owner);

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
     * @param owner Player whose inventory is being swapped with.
     * @param tracker Player who's performing the swapping.
     * @param amount  An object representing the amount of items to swap. Use
     *                {@link ItemSwapAmount#getNumItemsToSwap()}, passing in the item stack size of the item in the
     *                player's hand to get the amount of items to swap.
     */
    void swap(int slot, InteractionHand hand, ServerPlayer owner, ServerPlayer tracker, ItemSwapAmount amount);

    /**
     * Function to determine whether the Immersive's data has changed its contents to sync to the client since its last
     * sync. Dirtiness is addressed every tick, so for every tick, you should clear ALL flags used for dirtiness.
     * @param tracker Player to potentially send new data to.
     * @param owner Player whose data may be dirty.
     * @return Whether this Immersive for the provided player has changed since it was last synced.
     */
    boolean isDirtyForClientSync(ServerPlayer tracker, ServerPlayer owner);

    /**
     * Function called whenever this immersive is no longer being tracked. Only ever called server-side.
     * @param tracker Player who is no longer tracking this Immersive.
     * @param owner Player who owns/controls this Immersive.
     */
    default void onStopTracking(ServerPlayer tracker, ServerPlayer owner) {}

}
