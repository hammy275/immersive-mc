package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.common.api_impl.ImmersiveLogicHelpersImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

/**
 * Contains helpful methods that ensure the ImmersiveMC "style" is kept throughout API-implementors that wish
 * to implement it, while also making the life of API-implementors easier.
 */
public interface ImmersiveLogicHelpers {

    /**
     * @return An ImmersiveLogicHelpers instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveLogicHelpers instance() {
        return ImmersiveLogicHelpersImpl.INSTANCE;
    }

    /**
     * Given a player and the position of an immersive block, returns the best direction the block should face to be
     * looking towards the player. This is most commonly used for blocks like the crafting table which don't face a
     * direction to come up with a good estimation. This method will always return NORTH, EAST, SOUTH, or WEST.
     *
     * @param player The player
     * @param blockPos The block to determine the facing direction of.
     * @return A direction the block is facing, excluding UP and DOWN.
     */
    public Direction getHorizontalBlockForward(Player player, BlockPos blockPos);


}
