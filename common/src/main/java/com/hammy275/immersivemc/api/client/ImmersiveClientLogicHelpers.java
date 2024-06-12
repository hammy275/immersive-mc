package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.client.api_impl.ImmersiveClientLogicHelpersImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * {@link ImmersiveLogicHelpers}, but the player is always assumed to be the local player, and the world/level is
 * the one that player occupies. This also contains methods not found in {@link ImmersiveLogicHelpers}, as they
 * should only run on the client.
 */
public interface ImmersiveClientLogicHelpers extends ImmersiveLogicHelpers {

    /**
     * @return An ImmersiveClientLogicHelpers instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveClientLogicHelpers instance() {
        return ImmersiveClientLogicHelpersImpl.INSTANCE;
    }

    /**
     * Given the local player and the position of an immersive block, returns the best direction the block should face
     * to be looking towards the player. This is most commonly used for blocks like the crafting table which don't face
     * a direction to come up with a good estimation. This method will always return NORTH, EAST, SOUTH, or WEST.
     *
     * @param blockPos The block to determine the facing direction of.
     * @return A direction the block is facing, excluding UP and DOWN.
     */
    default Direction getHorizontalBlockForward(BlockPos blockPos) {
        return this.getHorizontalBlockForward(Minecraft.getInstance().player, blockPos);
    }

    /**
     * Get the intended light level to render with at the given position as packed sky light and block light. This
     * may not necessarily return the actual light level at the given block, though it should be treated as such.
     * @param pos The position to get the intended light level for rendering at.
     * @return The intended light level for rendering.
     */
    public int getLight(BlockPos pos);

    /**
     * Get the intended light level to render with given several positions. The value returned is a packed sky light
     * and block light, and intentionally may not necessarily return data consistent with all the positions, though
     * should be treated as such.
     * @param positions The positions to get the intended light rendering for.
     * @return The intended light level for rendering.
     */
    public int getLight(Iterable<BlockPos> positions);
}
