package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfoBuilder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HitboxVRMovementInfoBuilderImpl implements HitboxVRMovementInfoBuilder {

    @Nullable
    private Direction.Axis axis = null;
    private final double[] thresholds = new double[]{0, 0};
    private ControllerMode controllerMode = ControllerMode.EITHER;
    private Consumer<BuiltImmersiveInfo<?>> actionConsumer = ignored -> {};

    @Override
    public HitboxVRMovementInfoBuilder axis(@Nullable Direction.Axis axis) {
        this.axis = axis;
        return this;
    }

    @Override
    public HitboxVRMovementInfoBuilder threshold(double threshold) {
        if (threshold == 0) {
            thresholds[0] = 0;
            thresholds[1] = 0;
        } else if (threshold > 0) {
            thresholds[0] = threshold;
        } else {
            thresholds[1] = threshold;
        }
        return this;
    }

    @Override
    public HitboxVRMovementInfoBuilder controllerMode(ControllerMode controllerMode) {
        this.controllerMode = controllerMode;
        return this;
    }

    @Override
    public HitboxVRMovementInfoBuilder actionConsumer(Consumer<BuiltImmersiveInfo<?>> actionConsumer) {
        this.actionConsumer = actionConsumer;
        return this;
    }

    @Override
    public HitboxVRMovementInfo build() {
        return new HitboxVRMovementInfoImpl(this.axis, this.thresholds, this.controllerMode, this.actionConsumer);
    }
}
