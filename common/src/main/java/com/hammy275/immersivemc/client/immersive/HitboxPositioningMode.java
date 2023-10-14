package com.hammy275.immersivemc.client.immersive;

public enum HitboxPositioningMode {
    HORIZONTAL_BLOCK_FACING, // Face that block is facing, excluding up and down face
    PLAYER_FACING, // Top of block face, oriented on player look direction
    TOP_LITERAL, // Top of block face, literal orientation (x is always in-game x, etc.)
    TOP_BLOCK_FACING // Top of block face, oriented on block facing direction, excluding up and down face. Items face to player when rendering.
}
