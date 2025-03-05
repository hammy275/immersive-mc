package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfoBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public record HitboxVRMovementInfoImpl(@Nullable Direction.Axis relativeAxis, double[] thresholds,
                                       HitboxVRMovementInfoBuilder.ControllerMode controllerMode, BiConsumer<BuiltImmersiveInfo<?>, List<InteractionHand>> actionConsumer)
        implements HitboxVRMovementInfo {}
