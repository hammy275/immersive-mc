package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.BlockPos;

/**
 * ImmersiveInfos are effectively containers of data for {@link BlockBasedImmersive}s. For example, with the furnace,
 * there is one {@link BlockBasedImmersive} instance, which declares how to handle rendering furnaces, interacting with
 * hitboxes, etc. Meanwhile, an ImmersiveInfo instance exists for each furnace that is being rendered in the world,
 * containing data such as what item it contains, where in the world that furnace is, etc.
 * <p>
 * Note that although ImmersiveInfos generally hold info needed for rendering, the actual rendering data is extracted
 * from ImmersiveInfos into {@link ImmersiveRenderState} using methods such as {@link BlockBasedImmersive#extractRenderState}.
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
