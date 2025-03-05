package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * An object representing VR movement detection. These can be built from {@link HitboxVRMovementInfoBuilder}, which you
 * can see for more info.
 */
public interface HitboxVRMovementInfo {

    /**
     * @return The relative axis to detect movements on.
     */
    public Direction.Axis relativeAxis();

    /**
     * @return An array containing the threshold for the positive direction and the threshold for the negative
     * direction in that order.
     */
    public double[] thresholds();

    /**
     * @return The controller mode.
     */
    public HitboxVRMovementInfoBuilder.ControllerMode controllerMode();

    /**
     * @return The consumer to run when a threshold is met.
     */
    public BiConsumer<BuiltImmersiveInfo<?>, List<InteractionHand>> actionConsumer();
}
