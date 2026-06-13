package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.BlockPos;

/**
 * The {@link ImmersiveInfo} implementation for block-based Immersives.
 *
 * @see BlockBasedImmersive Information on block-based Immersives.
 * @see ImmersiveInfo Information on what ImmersiveInfos are.
 */
public non-sealed interface BlockBasedImmersiveInfo extends ImmersiveInfo {

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

}
