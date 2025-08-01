package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The result of swapping {@link ItemStack}s between the player and an Immersive, usually by interacting with
 * a hitbox of the Immersive that can hold items.
 */
public interface SwapResult {

    /**
     * @return The ItemStack that should be placed in the player's hand, overwriting what is currently in it.
     */
    public ItemStack playerHandStack();

    /**
     * @return The ItemStack that should be placed in the Immersive, overwriting what is currently in it.
     */
    public ItemStack immersiveStack();

    /**
     * @return The leftover items that should be given to the player.
     */
    public ItemStack leftoverStack();

    /**
     * Give the {@link #playerHandStack()} and {@link #leftoverStack()} to the player specified as appropriate. This
     * function should only be called once per swap operation.
     * <br>
     * The {@link #immersiveStack()} needs to be handled separately.
     * @param player The player to give items to.
     * @param hand The hand the player used to interact with the Immersive.
     */
    public void giveToPlayer(Player player, InteractionHand hand);
}
