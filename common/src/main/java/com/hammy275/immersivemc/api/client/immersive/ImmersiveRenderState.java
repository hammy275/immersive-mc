package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;

import java.util.List;

/**
 * State for rendering a {@link ImmersiveInfo}. Unlike ImmersiveInfos, these classes only need to hold information
 * related to rendering.
 * <p>
 * Importantly, data in render states should be independent of data in the infos they come from.
 *
 * @see BlockBasedImmersive#extractRenderState More information on the independence of data between implementations of
 *                                             this interface and the {@link ImmersiveInfo}s from which they originate.
 */
public interface ImmersiveRenderState {

    /**
     * Gets the list of all hitboxes for interaction, this interface's counterpart to
     * {@link BlockBasedImmersiveInfo#getAllHitboxes()}. Like its counterpart, the list can contain null elements.
     * <p>
     * For Immersives that have hitboxes
     * @return A list of all bounding boxes indexed by their slot number.
     */
    public List<BoundingBox> hitboxes();

    /**
     * Gets the amount of ticks the info this render state comes from has existed, this interface's counterpart to
     * {@link BlockBasedImmersiveInfo#getTicksExisted()}.
     * @return The amount of ticks the ImmersiveInfo this render state came from has existed.
     */
    public long ticksExisted();

    /**
     * Gets whether the provided slot (index into {@link #hitboxes()}) is currently hovered. This is similar to
     * {@link BlockBasedImmersiveInfo#getSlotHovered(int)}, but this function gets the slot and returns whether that slot
     * is hovered, whereas {@link BlockBasedImmersiveInfo#getSlotHovered(int)} gets the hand index and returns the hovered
     * slot.
     * @param slot The slot/index into {@link #hitboxes()} that is checked if it's hovered.
     * @return Whether the aforementioned slot is hovered.
     */
    public boolean isSlotHovered(int slot);

}
