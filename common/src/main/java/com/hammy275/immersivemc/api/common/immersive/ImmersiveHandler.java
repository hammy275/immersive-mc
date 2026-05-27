package com.hammy275.immersivemc.api.common.immersive;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public sealed interface ImmersiveHandler permits BlockBasedImmersiveHandler {
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
    Identifier getID();
}
