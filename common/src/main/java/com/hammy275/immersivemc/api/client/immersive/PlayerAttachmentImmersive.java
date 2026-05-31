package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import net.minecraft.client.player.AbstractClientPlayer;

import javax.annotation.Nullable;

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

    /**
     * @return The PlayerAttachmentImmersiveInfo for the local player for this Immersive, or null if this Immersive
     * isn't active for the local player.
     */
    @Nullable
    default I getLocalPlayerInfo() {
        for (I info : getTrackedObjects()) {
            if (info.ownerIsLocalPlayer()) {
                return info;
            }
        }
        return null;
    }
}
