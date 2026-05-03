package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;

import java.util.List;

public interface ImmersiveRenderState {

    public List<BoundingBox> hitboxes();

    public long ticksExisted();

    public boolean isSlotHovered(int slot);

}
