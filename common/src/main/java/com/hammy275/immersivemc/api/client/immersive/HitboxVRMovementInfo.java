package com.hammy275.immersivemc.api.client.immersive;

import com.google.common.annotations.Beta;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Beta
public record HitboxVRMovementInfo(@Nullable Direction.Axis relativeAxis, double[] thresholds,
                                   ControllerMode controllerMode, Consumer<BuiltImmersiveInfo<?>> action) {

    public enum ControllerMode {
        C0, C1, EITHER, BOTH
    }
}
