package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The {@link ImmersiveInfo} implementation for player-attachment Immersives.
 *
 * @see BlockBasedImmersive Information on player-attachment Immersives.
 * @see ImmersiveInfo Information on what ImmersiveInfos are.
 */
public non-sealed interface PlayerAttachmentImmersiveInfo extends ImmersiveInfo {

    /**
     * Get the player who owns/controls this PlayerAttachmentImmersiveInfo.
     *
     * @return The player as described.
     */
    public AbstractClientPlayer getOwner();

    /**
     * Get whether this info is owned/controlled by the local player.
     * @return Whether the player that owns/controls this info is the local player.
     */
    default boolean ownerIsLocalPlayer() {
        return getOwner().isLocalPlayer();
    }

}
