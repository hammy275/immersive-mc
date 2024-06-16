package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.Direction;

/**
 * Enum used for vertical-forcing the direction of an item being rendered in the world. This is mainly used with
 * {@link ImmersiveBuilder}s. The options available are:
 * <ul>
 *     <li>UP - Forces items to render facing towards the sky.</li>
 *     <li>DOWN - Forces items to render facing towards the ground.</li>
 *     <li>NULL - Items are not made to stay facing up or down.</li>
 *     <li>NOT_FORCED - The forced rendering direction is determined by the positioning mode set by</li>
 * </ul>
 * {@link ImmersiveBuilder#setPositioningMode(HitboxPositioningMode)}.
 */
public enum ForcedUpDownRenderDir {

    UP(Direction.UP), DOWN(Direction.DOWN), NULL(null), NOT_FORCED(null);

    public final Direction direction;

    ForcedUpDownRenderDir(Direction dir) {
        this.direction = dir;
    }
}
