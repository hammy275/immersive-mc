package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.client.immersive.HitboxVRMovementInfoBuilderImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public interface HitboxVRMovementInfoBuilder {

    /**
     * Starts creation of a {@link HitboxVRMovementInfo}.
     * @return A builder object to begin creation.
     */
    public static HitboxVRMovementInfoBuilder create() {
        return new HitboxVRMovementInfoBuilderImpl();
    }

    /**
     * Sets the axis of movement for the HitboxVRMovementInfo.
     * @param axis The axis of movement to detect motion on, or null to detect on all axes combined.
     * @return Builder object.
     */
    public HitboxVRMovementInfoBuilder axis(@Nullable Direction.Axis axis);

    /**
     * Sets the amount of distance that should be covered within a game tick for the movement to have succeeded.
     * <br>
     * One positive and one negative threshold can be set. Future sets will override the value already there. For
     * example, setting the thresholds 0.03, -0.02, and 0.02, will have the thresholds for movement detection be 0.02
     * positive movement and 0.02 negative movement.
     * <br>
     * Setting a threshold of 0 will clear both the positive and negative thresholds.
     * @param threshold Threshold of movement.
     * @return Builder object.
     */
    public HitboxVRMovementInfoBuilder threshold(double threshold);

    /**
     * Sets which controllers are detected for meeting the threshold of movement.
     * @param controllerMode Which controller(s) should be watched for meeting the movement threshold.
     * @return Builder object.
     */
    public HitboxVRMovementInfoBuilder controllerMode(ControllerMode controllerMode);

    /**
     * Sets a callback to run when the threshold is met.
     * @param actionConsumer The callback to run when the threshold is met, taking the info and the hand(s) that met the threshold.
     * @return Builder object.
     */
    public HitboxVRMovementInfoBuilder actionConsumer(BiConsumer<BuiltImmersiveInfo<?>, List<InteractionHand>> actionConsumer);

    /**
     * Builds this builder into a {@link HitboxVRMovementInfo}.
     * @return The built HitboxVRMovementInfo.
     */
    public HitboxVRMovementInfo build();


    /**
     * The controllers to detect for passing the threshold for movement.
     */
    public enum ControllerMode {
        PRIMARY, // Only the primary controller is checked.
        SECONDARY, // Only the secondary controller is checked.
        EITHER, // Both controllers are checked and only one needs to meet the threshold.
        BOTH // Both controllers are checked and both need to meet the threshold.
    }
}
