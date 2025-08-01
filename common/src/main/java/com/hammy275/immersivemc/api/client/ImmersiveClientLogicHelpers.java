package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.client.api_impl.ImmersiveClientLogicHelpersImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.List;

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
     * Sets both the vanilla, right-click cooldown and ImmersiveMC's VR cooldown for interacting with Immersives
     * (if the player is in VR) to some number of ticks. You likely don't need this, as the value returned from
     * {@link Immersive#handleHitboxInteract(ImmersiveInfo, LocalPlayer, List, InteractionHand, boolean)} is set as the cooldown
     * where appropriate. This is mainly useful if you're working outside of ImmersiveMC's hitbox system.
     * @param cooldown The cooldown to set in ticks. This will be increased for VR players, see the aforementioned
     *                 method for more info.
     */
    public void setCooldown(int cooldown);

    /**
     * Sends the packet to the server telling it to run
     * {@link com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler#swap(int, InteractionHand, BlockPos, ServerPlayer, ItemSwapAmount)}
     * for the provided block at the given position. You usually should call this when a hitbox is right-clicked in your
     * Immersive.
     *
     * @param pos The position of the block that a swap is being performed at.
     * @param slots The slot numbers that the right-click is taking place for.
     * @param hand The hand which is performing the swap.
     * @param modifierPressed Whether the modifier key (usually the button mapped to breaking blocks) was held for the
     *                        interaction.
     */
    public void sendSwapPacket(BlockPos pos, List<Integer> slots, InteractionHand hand, boolean modifierPressed);

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
