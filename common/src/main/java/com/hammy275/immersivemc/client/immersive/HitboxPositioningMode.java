package com.hammy275.immersivemc.client.immersive;

public enum HitboxPositioningMode {
    HORIZONTAL_BLOCK_FACING, // Face that block is facing, excluding up and down face
    TOP_PLAYER_FACING, // Top of block face, oriented on player look direction
    TOP_LITERAL, // Top of block face, literal orientation (x is always in-game x, etc.)
    TOP_BLOCK_FACING, // Top of block face, oriented on block facing direction, excluding up and down face. Items face to player when rendering.
    HORIZONTAL_PLAYER_FACING, // Face that faces the player, excluding up and down face.
    BLOCK_FACING_NEG_X, // Same as HORIZONTAL_BLOCK_FACING, but negative X in-world corresponds to +x during translation if the block faces up or down
    PLAYER_FACING_NO_DOWN // Combination of HORIZONTAL_PLAYER_FACING and TOP_PLAYER_FACING
}
