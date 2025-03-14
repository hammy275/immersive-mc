package com.hammy275.immersivemc.api.client.immersive;

/**
 * The type of relative positioning to use for an Immersive built with {@link ImmersiveBuilder}. The API guarantees the
 * below modes won't be removed and won't change names in terms of backwards-compatibility. Anything else, including
 * the ordering of the modes and the amount of them are NOT part of the API, and may change at anytime.
 * <br>
 * Explanations and an example of how ImmersiveMC uses each mode:
 * <ul>
 *     <li>HORIZONTAL_BLOCK_FACING: Positioning is done based on the direction the block faces, where the block does
 *     not face up or down. Positioning starts from the center of this face. From the perspective of a player facing the
 *     front of this block, +X moves the hitbox to the player's right, +Y moves the hitbox up on the world's Y axis, and
 *     +Z moves the hitbox towards the player. For example, ImmersiveMC uses this for furnaces.</li>
 *
 *     <li>TOP_PLAYER_FACING: Positioning is done from the top-center of the block, with the direction being
 *     based on the player's direction relative to the block, excluding UP and DOWN.
 *     From the perspective of a player facing the block from a side, +X moves the hitbox to the player's right, +Y moves
 *     the hitbox in the same direction the player is facing, and +Z moves the hitbox up on the world's Y axis. For
 *     example, ImmersiveMC uses this for the crafting table.</li>
 *
 *     <li>TOP_LITERAL: Positioning is done from the top-center of the block. All translations are done in literal
 *     world movements (+X moves on the X-axis positively, etc.). ImmersiveMC uses this for the jukebox.</li>
 *
 *     <li>TOP_BLOCK_FACING: The same as TOP_PLAYER_FACING, but the direction is based on the block's facing
 *     direction, rather than the player relative to the block. The block must not face vertically. ImmersiveMC uses
 *     this for the anvil.</li>
 *
 *     <li>HORIZONTAL_PLAYER_FACING: The same as HORIZONTAL_BLOCK_FACING, but positioning is done based on the
 *     player's direction relative to the block, like in TOP_PLAYER_FACING. ImmersiveMC uses this for the brewing
 *     stand.</li>
 *
 *     <li>BLOCK_FACING_NEG_X: The same as HORIZONTAL_BLOCK_FACING, but if the block faces up or down, calculations
 *     are done assuming the block faces south. This makes +X correspond to moving the hitbox west. ImmersiveMC
 *     uses this for the barrel, mainly for positioning the handle.</li>
 *
 *     <li>PLAYER_FACING_NO_DOWN: TOP_PLAYER_FACING if the player is above the block, and HORIZONTAL_PLAYER_FACING
 *     otherwise. ImmersiveMC uses this for the hopper.</li>
 *
 *     <li>PLAYER_FACING_FILTER_BLOCK_FACING: Positioning is done from the center of the block face opposite of the
 *     direction the player is facing, with relative movements are done from the next-closest direction. For example,
 *     if the player were facing west towards the block on a similar y-level as the block, the positioning starts from
 *     center of the eastern face of the block. In this example +X corresponds to moving the hitbox to the player's
 *     right, +Y corresponds to moving the hitbox up on the world's y-axis, and +Z moves the hitbox towards the player.
 *     ImmersiveMC uses this for the shulker box.</li>
 * </ul>
 */
public enum HitboxPositioningMode {
    HORIZONTAL_BLOCK_FACING, // Face that block is facing, excluding up and down face
    TOP_PLAYER_FACING, // Top of block face, oriented on player look direction
    TOP_LITERAL, // Top of block face, literal orientation (x is always in-game x, etc.)
    TOP_BLOCK_FACING, // Top of block face, oriented on block facing direction, excluding up and down face.
    HORIZONTAL_PLAYER_FACING, // Face that faces the player, excluding up and down face.
    BLOCK_FACING_NEG_X, // Same as HORIZONTAL_BLOCK_FACING, but negative X in-world corresponds to +x during translation if the block faces up or down
    PLAYER_FACING_NO_DOWN, // Combination of HORIZONTAL_PLAYER_FACING and TOP_PLAYER_FACING
    PLAYER_FACING_FILTER_BLOCK_FACING, // Effectively PLAYER_FACING (assuming it were to exist) except the axis the block is facing is ignored. If the block is facing down, PLAYER_FACING NESW only.
    HORIZONTAL_BLOCK_FACING_ATTACHED_FLOOR_CEILING_REVERSED, // HORIZONTAL_BLOCK_FACING, but the front is based on the attachment point. Additionally, non-wall attachments treat the block as facing the opposite direction. Equivalent to HORIZONTAL_BLOCK_FACING when attached to the floor but with the direction reversed.
}
