package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the client-side implementation of a player-attachment Immersive implementation. One should implement this
 * interface to create the client-side implementation of a player-attachment Immersive.
 * <p>
 * When combined with a {@link PlayerAttachmentImmersiveHandler}, you have a fully-functioning player-attachment
 * Immersive!
 * @param <I> The {@link BlockBasedImmersiveInfo} implementation this Immersive uses.
 * @param <R> The render state implementation this Immersive uses. See {@link Immersive#extractRenderState}.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 *
 * @see Immersive Information on what an Immersive is.
 */
public non-sealed interface PlayerAttachmentImmersive<I extends PlayerAttachmentImmersiveInfo, R extends ImmersiveRenderState, S extends NetworkStorage>
        extends Immersive<I, R, S> {

    /**
     * Constructs a new PlayerAttachmentImmersiveInfo based on the provided owning player. It's best to calculate
     * initial hitboxes, etc. in this method to make the Immersive is available for interaction as soon as possible.
     * <p>
     * This method is only called if @link ImmersiveHandler#clientAuthoritative()} returns false for the
     * {@link PlayerAttachmentImmersiveHandler} returned by {@link #getHandler()}.
     *
     * @param player The player who owns/controls this Immersive. This may not necessarily be the same player as the
     *               local player!
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
