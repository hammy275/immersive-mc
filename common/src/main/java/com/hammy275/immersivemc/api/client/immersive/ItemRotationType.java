package com.hammy275.immersivemc.api.client.immersive;

import com.google.common.annotations.Beta;
import net.minecraft.core.Direction;

import java.util.function.Function;

/**
 * Item rotation type for {@link RelativeHitboxInfoBuilder#rotateItem(ItemRotationType)}.
 */
@Beta
public enum ItemRotationType {
    CLOCKWISE(Direction::getClockWise),
    COUNTERCLOCKWISE(Direction::getCounterClockWise),
    OPPOSITE(Direction::getOpposite);

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
