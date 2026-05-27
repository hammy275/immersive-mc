package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Represents the client-side implementation of a block-based Immersive implementation. One should implement this
 * interface to create the client-side implementation of a block-based Immersive (or, alternatively, build one using an
 * {@link BlockBasedImmersiveBuilder}).
 * <p>
 * When combined with a {@link BlockBasedImmersiveHandler}, you have a fully-functioning block-based Immersive!
 * @param <I> The {@link BlockBasedImmersiveInfo} implementation this Immersive uses.
 * @param <R> The render state implementation this Immersive uses. See {@link Immersive#extractRenderState}.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 */
public non-sealed interface BlockBasedImmersive<I extends BlockBasedImmersiveInfo, R extends ImmersiveRenderState, S extends NetworkStorage>
        extends Immersive<I, R, S, BlockBasedImmersiveHandler<S>> {

    /**
     * Constructs a new ImmersiveInfo based on the provided block position. It's best to calculate initial hitboxes,
     * etc. in this method to make the Immersive available for interaction as soon as possible.
     *
     * @param pos The position of a block that matches this Immersive.
     * @param level The level in which this info is being built.
     * @return An instance of an ImmersiveInfo implementation with the same position as provided.
     */
    public I buildInfo(BlockPos pos, Level level);

    /**
     * Whether normal right-click behavior for this block should be disabled when the option to disable interactions is
     * enabled in ImmersiveMC. This should usually return true if the block opens a GUI on right-click.
     * @param info The info for the block that may want to disable right-clicks.
     * @return Whether to skip right-click behavior for this block when the option to disable click interactions is
     *         enabled in ImmersiveMC.
     */
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(I info);

}
