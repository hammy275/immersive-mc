package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * While an {@link com.hammy275.immersivemc.api.client.immersive.Immersive} defines how a client should work
 * with an Immersive, an ImmersiveHandler handles both the server-half of a block-based Immersive, along
 * with the shared, common portion. For example, retrieving what items are stored in a furnace to send to the client
 * (server-specific) and identifying what a furnace is (common) are both handled in an ImmersiveHandler.
 *
 * @see BlockBasedImmersiveHandler The subinterface of this interface for block-based Immersives.
 * @see PlayerAttachmentImmersiveHandler The subinterface of this interface for player-attachment Immersives.
 */
public sealed interface ImmersiveHandler permits BlockBasedImmersiveHandler, PlayerAttachmentImmersiveHandler {
    /**
     * @param player The player we're checking the config of.
     * @return Whether the immersive this handler handles is enabled. If you do not have a configuration system, this
     * should always return true.
     */
    boolean enabledInConfig(Player player);

    /**
     * Whether the client should be the one to begin tracking this immersive. If this is true, the server should
     * not send any data to the client about this Immersive, and does not know who is tracking this Immersive.
     *
     * @return Whether this immersive should have tracking initiated by the client. The same value should always be
     *         returned by this method.
     */
    boolean clientAuthoritative();

    /**
     * @return A unique ID to identify this handler. The same value should always be returned by this method.
     */
    ResourceLocation getID();
}
