package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapResult;
import com.hammy275.immersivemc.common.api_impl.ImmersiveLogicHelpersImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Contains helpful methods that ensure the ImmersiveMC "style" is kept throughout API-implementors that wish
 * to implement it, while also making the life of API-implementors easier.
 * <br>
 * These functions may be useful on the client and/or the server.
 */
public interface ImmersiveLogicHelpers {

    /**
     * @return An ImmersiveLogicHelpers instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveLogicHelpers instance() {
        return ImmersiveLogicHelpersImpl.INSTANCE;
    }

    /**
     * Given a player and the position of an immersive block, returns the best direction the block should face to be
     * looking towards the player. This is most commonly used for blocks like the crafting table which don't face a
     * direction to come up with a good estimation. This method will always return NORTH, EAST, SOUTH, or WEST.
     *
     * @param player The player
     * @param blockPos The block to determine the facing direction of.
     * @return A direction the block is facing, excluding UP and DOWN.
     */
    public Direction getHorizontalBlockForward(Player player, BlockPos blockPos);

    /**
     * Gets the result of a swap action between a player and an Immersive where the player may place items into
     * and take items out of a slot, such as the input slots of a furnace.
     * @param stackFromPlayer The {@link ItemStack} currently in the player's hand.
     * @param stackInImmersive The {@link ItemStack} currently in the slot of the Immersive the player is interacting
     *                         with.
     * @param swapAmount The {@link ItemSwapAmount} that is determining the settings for how many items to swap.
     * @return A {@link SwapResult} containing the {@link ItemStack}s that should be placed in the player's hand, be
     *         placed into the slot of the Immersive being interacted with, and the leftovers that should be given to
     *         the player some other way.
     */
    public SwapResult swapItems(ItemStack stackFromPlayer, ItemStack stackInImmersive, ItemSwapAmount swapAmount, Player player);

    /**
     * Gets the result of a swap action between a player and an Immersive where the player may place items into
     * and take items out of a slot, such as the input slots of a furnace.
     * @param stackFromPlayer The {@link ItemStack} currently in the player's hand.
     * @param stackInImmersive The {@link ItemStack} currently in the slot of the Immersive the player is interacting
     *                         with.
     * @param swapAmount The {@link ItemSwapAmount} that is determining the settings for how many items to swap.
     * @param player The player performing the swap.
     * @param forcedMaxImmersiveStackSize A forced maximum stack size for the slot in the Immersive, or -1 to use the item's stack size.
     * @return A {@link SwapResult} containing the {@link ItemStack}s that should be placed in the player's hand, be
     *         placed into the slot of the Immersive being interacted with, and the leftovers that should be given to
     *         the player some other way.
     */
    public SwapResult swapItems(ItemStack stackFromPlayer, ItemStack stackInImmersive, ItemSwapAmount swapAmount, Player player, int forcedMaxImmersiveStackSize);

    /**
     * Gets the result of a swap action between a player and an Immersive where the player may place items only into
     * a slot, such as the output slot of a furnace.
     * @param stackFromPlayer The {@link ItemStack} currently in the player's hand.
     * @param stackInImmersive The {@link ItemStack} currently in the output slot of the Immersive the player is
     *                         interacting with.
     * @param player The player performing the swap.
     * @return A {@link SwapResult} containing the {@link ItemStack}s that should be placed in the player's hand, be
     *         placed into the slot of the Immersive being interacted with, and the leftovers that should be given to
     *         the player some other way.
     */
    public SwapResult swapItemsWithOutput(ItemStack stackFromPlayer, ItemStack stackInImmersive, Player player);
}
