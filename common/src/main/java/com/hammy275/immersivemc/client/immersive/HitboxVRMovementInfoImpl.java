package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfoBuilder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record HitboxVRMovementInfoImpl(@Nullable Direction.Axis relativeAxis, double[] thresholds,
                                       HitboxVRMovementInfoBuilder.ControllerMode controllerMode, Consumer<BuiltImmersiveInfo<?>> actionConsumer)
        implements HitboxVRMovementInfo {}
