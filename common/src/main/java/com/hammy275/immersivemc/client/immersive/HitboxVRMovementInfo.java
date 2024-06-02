package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfoImpl;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record HitboxVRMovementInfo(@Nullable Direction.Axis relativeAxis, double[] thresholds,
                                   ControllerMode controllerMode, Consumer<BuiltImmersiveInfoImpl> action) {

    public enum ControllerMode {
        C0, C1, EITHER, BOTH
    }
}
