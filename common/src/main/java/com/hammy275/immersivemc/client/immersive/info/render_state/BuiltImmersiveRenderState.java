package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;

public class BuiltImmersiveRenderState<ER> implements ImmersiveRenderState {

    public List<RelativeHitboxRenderState> hitboxes;
    public long ticksExisted;
    public boolean airCheckPassed;
    public int light;
    public Direction immersiveDir;
    public AABB dragHitbox;
    public ER extraData;

    public boolean hasHitboxes() {
        return hitboxes.stream().anyMatch(Objects::nonNull);
    }

    @Override
    public List<BoundingBox> hitboxes() {
        return hitboxes.stream().map(hitbox -> hitbox.aabb).toList();
    }

    @Override
    public long ticksExisted() {
        return ticksExisted;
    }

    @Override
    public boolean isSlotHovered(int slot) {
    }
}
