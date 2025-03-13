package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfoBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public class HitboxVRMovementInfoBuilderImpl implements HitboxVRMovementInfoBuilder {

    @Nullable
    private Direction.Axis axis = null;
    private double[] thresholds = new double[]{0, 0};
    private ControllerMode controllerMode = ControllerMode.EITHER;
    private BiConsumer<BuiltImmersiveInfo<?>, List<InteractionHand>> actionConsumer = (ignored, ignored2) -> {};

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
    public HitboxVRMovementInfoBuilder actionConsumer(BiConsumer<BuiltImmersiveInfo<?>, List<InteractionHand>> actionConsumer) {
        this.actionConsumer = actionConsumer;
        return this;
    }

    @Override
    public HitboxVRMovementInfo build() {
        if (this.axis == null) {
            this.thresholds = new double[]{Math.abs(Math.max(this.thresholds[0], this.thresholds[1]))};
        } else {
            if (this.thresholds[0] == 0 && this.thresholds[1] != 0) {
                this.thresholds = new double[]{this.thresholds[1]};
            } else if (this.thresholds[0] != 0 && this.thresholds[1] == 0) {
                this.thresholds = new double[]{this.thresholds[0]};
            }
        }
        return new HitboxVRMovementInfoImpl(this.axis, this.thresholds, this.controllerMode, this.actionConsumer);
    }
}
