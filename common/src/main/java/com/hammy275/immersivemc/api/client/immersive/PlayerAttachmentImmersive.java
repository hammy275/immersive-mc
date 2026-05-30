package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import net.minecraft.client.player.AbstractClientPlayer;

public non-sealed interface PlayerAttachmentImmersive<I extends PlayerAttachmentImmersiveInfo, R extends ImmersiveRenderState, S extends NetworkStorage>
        extends Immersive<I, R, S> {

    /**
     * Constructs a new PlayerAttachmentImmersiveInfo based on the provided owning player. It's best to calculate
     * initial hitboxes, etc. in this method to make the Immersive is available for interaction as soon as possible.
     *
     * @param player The player who owns/controls this Immersive. For Immersives where
     *               {@link ImmersiveHandler#clientAuthoritative()} returns false, this may be a player other than
     *               the local player.
     * @return An instance of an ImmersiveInfo implementation with the same position as provided.
     */
    public I buildInfo(AbstractClientPlayer player);

    /**
     * @return The {@link PlayerAttachmentImmersiveHandler} this Immersive uses.
     */
    PlayerAttachmentImmersiveHandler<S> getHandler();
}
