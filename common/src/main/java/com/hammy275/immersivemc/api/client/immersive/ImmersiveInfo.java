package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import net.minecraft.core.BlockPos;

import java.util.List;

public interface ImmersiveInfo {

    /**
     * @return The list of all hitboxes this Immersive uses. This can contain null elements, and can return an
     *         immutable list implementation if desired.
     */
    public List<HitboxInfo> getAllHitboxes();

    /**
     * Whether this ImmersiveInfo contains valid hitboxes that are ready for use by users in-game.
     * {@link #getAllHitboxes()} will not be called if this method returns false for a given tick.
     * @return Whether {@link #getAllHitboxes()} can be safely called and contains expected data.
     */
    public boolean hasHitboxes();

    /**
     * Gets the block position of the block this ImmersiveInfo represents. This function should always return the same
     * value for an individual ImmersiveInfo instance, and this function may be called after the block at this
     * position no longer matches the Immersive it represents.
     * <br>
     * For example, if this ImmersiveInfo was used to represent a furnace that was initially placed at x=1, y=2,
     * and z=3, this function should always return the position x=1, y=2, and z=3, even after the furnace is destroyed
     * or replaced by some other block.
     * @return The position of the block this ImmersiveInfo represents.
     */
    public BlockPos getBlockPosition();

    /**
     * A notification to mark the given slot as hovered by the given hand index.
     * @param hitboxIndex The index into {@link #getAllHitboxes()} to mark as hovered, or -1 to indicate no slot is
     *                  hovered by this hand.
     * @param handIndex 0 for the primary hand, and 1 for the secondary hand.
     */
    public void setSlotHovered(int hitboxIndex, int handIndex);

    /**
     * @param hitboxIndex The index into {@link ImmersiveInfo#getAllHitboxes()} to check.
     * @return Whether the given slotIndex is being hovered by any hand.
     */
    public boolean isSlotHovered(int hitboxIndex);

    /**
     * @return The number of ticks this info has existed for.
     */
    public long getTicksExisted();
}
