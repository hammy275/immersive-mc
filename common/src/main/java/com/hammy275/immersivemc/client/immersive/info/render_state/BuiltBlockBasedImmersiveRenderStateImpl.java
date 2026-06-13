package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BuiltBlockBasedImmersiveRenderStateImpl<ER> implements BuiltImmersiveRenderState<ER> {

    public List<RelativeHitboxRenderState> hitboxes;
    public BlockPos pos;
    public long ticksExisted;
    public boolean airCheckPassed;
    public int light;
    public Direction immersiveDir;
    public AABB dragHitbox;
    public @Nullable ER extraData;
    public int[] slotsHovered;

    public boolean hasHitboxes() {
        return hitboxes.stream().anyMatch(Objects::nonNull);
    }

    @Override
    public List<BoundingBox> hitboxes() {
        return hitboxes.stream().map(hitbox -> (BoundingBox) hitbox.aabb).toList();
    }

    @Override
    public long ticksExisted() {
        return ticksExisted;
    }

    @Override
    public boolean isSlotHovered(int slot) {
        return slotsHovered[0] == slot || slotsHovered[1] == slot || SwapTracker.slotHovered(this, slot);
    }

    @Override
    public BlockPos getBlockPos() {
        return pos;
    }

    @Override
    @Nullable
    public ER getExtraRenderData() {
        return extraData;
    }
}
