package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;

import java.util.List;

/**
 * ImmersiveInfos are effectively containers of data for {@link Immersive}s. For example, with the furnace,
 * there is one {@link Immersive} instance, which declares how to handle rendering furnaces, interacting with
 * hitboxes, etc. Meanwhile, an ImmersiveInfo instance exists for each furnace that is being rendered in the world,
 * containing data such as what item it contains, where in the world that furnace is, etc.
 * <p>
 * Note that although ImmersiveInfos generally hold info needed for rendering, the actual rendering data is extracted
 * from ImmersiveInfos into {@link ImmersiveRenderState} objects using methods such as
 * {@link Immersive#extractRenderState}.
 *
 * @see BlockBasedImmersiveInfo The subinterface for block-based Immersives.
 * @see BuiltBlockBasedImmersiveInfo The subinterface for block-based Immersives built using the
 *                                   {@link BlockBasedImmersiveBuilder}.
 * @see PlayerAttachmentImmersiveInfo The subinterface for player-attachment Immersives.
 */
public sealed interface ImmersiveInfo permits BlockBasedImmersiveInfo, PlayerAttachmentImmersiveInfo {

    /**
     * @return The list of all hitboxes this Immersive uses. This can contain null elements, and can return an
     *         immutable list implementation if desired.
     */
    List<? extends HitboxInfo> getAllHitboxes();

    /**
     * Whether this ImmersiveInfo contains valid hitboxes that are ready for use by users in-game.
     * {@link #getAllHitboxes()} will not be called if this method returns false for a given tick.
     * @return Whether {@link #getAllHitboxes()} can be safely called and contains expected data.
     */
    boolean hasHitboxes();

    /**
     * A notification to mark the given slot as hovered by the given hand index.
     * @param hitboxIndex The index into {@link #getAllHitboxes()} to mark as hovered, or -1 to indicate no slot is
     *                  hovered by this hand.
     * @param handIndex 0 for the primary hand (primary controller in VR or the player hand in non-VR), and 1 for the
     *                  secondary hand (secondary controller in VR, or nothing in non-VR).
     */
    void setSlotHovered(int hitboxIndex, int handIndex);

    /**
     * @param handIndex The hand that is checking for a hovered hitbox.
     * @return The hitbox the handIndex is hovering, or -1 if it isn't hovering any slot.
     */
    int getSlotHovered(int handIndex);

    /**
     * @return The number of ticks this info has existed for.
     */
    long getTicksExisted();
}
