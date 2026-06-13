package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.Direction;

/**
 * Enum used for vertical-forcing the direction of an item being rendered in the world. This is mainly used with
 * {@link BlockBasedImmersiveBuilder#setPositioningMode(HitboxPositioningMode)}. The options available are:
 * <ul>
 *     <li>UP - </li>
 *     <li>DOWN - </li>
 *     <li>NULL - </li>
 *     <li>NOT_FORCED - </li>
 * </ul>
 */
public enum ForcedUpDownRenderDir {

    /**
     * Forces items to render facing towards the sky.
     */
    UP(Direction.UP),
    /**
     * Forces items to render facing towards the ground.
     */
    DOWN(Direction.DOWN),
    /**
     * Items are not made to stay facing up or down, even if the positioning mode would determine otherwise.
     */
    NULL(null),
    /**
     * The forced rendering direction is determined by the positioning mode set by.
     */
    NOT_FORCED(null);

    public final Direction direction;

    ForcedUpDownRenderDir(Direction dir) {
        this.direction = dir;
    }
}
