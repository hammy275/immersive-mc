package com.hammy275.immersivemc.api.client.immersive;

import com.google.common.annotations.Beta;
import net.minecraft.core.Direction;

import java.util.function.Function;

/**
 * Item rotation type for {@link RelativeHitboxInfoBuilder#rotateItem(ItemRotationType)}.
 */
@Beta
public enum ItemRotationType {
    /**
     * Rotate the item clockwise from the default rotation for the Immersive.
     */
    CLOCKWISE(Direction::getClockWise),
    /**
     * Rotate the item counterclockwise from the default rotation for the Immersive.
     */
    COUNTERCLOCKWISE(Direction::getCounterClockWise),
    /**
     * Rotate the item to face the opposite direction from the default rotation for the Immersive.
     */
    OPPOSITE(Direction::getOpposite);

    /**
     * Internal object that specifies how the rotation is performed. This is not covered under the API, and may change
     * in any way at any time!
     */
    private final Function<Direction, Direction> transformer;

    ItemRotationType(Function<Direction, Direction> transformer) {
        this.transformer = transformer;
    }

    /**
     * Transform a direction with the ItemRotationType.
     * @param direction The direction to transform.
     * @return The transformed direction.
     */
    public Direction transform(Direction direction) {
        return transformer.apply(direction);
    }
}
