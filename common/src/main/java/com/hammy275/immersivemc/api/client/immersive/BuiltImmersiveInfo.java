package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.world.item.ItemStack;

/**
 * An implementation of {@link ImmersiveInfo} provided by ImmersiveMC that's used with Immersives built via
 * {@link ImmersiveBuilder}. The hitboxIndex below always references the order in which hitboxes were added.
 */
public interface BuiltImmersiveInfo<E> extends ImmersiveInfo {

    /**
     * @return The number of times this Immersive has ticked.
     */
    public long ticksExisted();

    /**
     * @return The extra data instance attached to this info.
     */
    public E getExtraData();


    /**
     * Get the item stored at the given hitbox index. The item returned is an empty stack if the hitbox does not contain
     * an item.
     * @param hitboxIndex The hitbox index to get the item from.
     * @return The item in that hitbox.
     */
    public ItemStack getItem(int hitboxIndex);

    /**
     * Sets an item into the provided hitbox only on the client-side. A network sync may overwrite this data!
     * @param hitboxIndex The index to set an item in.
     * @param item The item to set.
     */
    public void setFakeItem(int hitboxIndex, ItemStack item);

    /**
     * Checks if the given hitbox is currently hovered by some hand or the mouse.
     * @param hitboxIndex The hitbox index to check if hovered.
     * @return Whether the given hitboxIndex is hovered.
     */
    public boolean isSlotHovered(int hitboxIndex);
}
