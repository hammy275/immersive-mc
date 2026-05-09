package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;

import java.util.List;

public class DragRenderState implements ImmersiveRenderState {

    public List<HitboxInfo> hitboxes;
    public long ticksExisted;
    public int startingHitboxIndex;

    @Override
    public List<BoundingBox> hitboxes() {
        return hitboxes.stream().map(HitboxInfo::getHitbox).toList();
    }

    @Override
    public long ticksExisted() {
        return ticksExisted;
    }

    @Override
    public boolean isSlotHovered(int slot) {
        return false;
    }
}
