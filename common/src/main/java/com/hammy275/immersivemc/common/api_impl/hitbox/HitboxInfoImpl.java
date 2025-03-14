package com.hammy275.immersivemc.common.api_impl.hitbox;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;

public class HitboxInfoImpl implements HitboxInfo {

    private final BoundingBox boundingBox;
    private final boolean isTriggerHitbox;

    public HitboxInfoImpl(BoundingBox boundingBox, boolean isTriggerHitbox) {
        this.boundingBox = boundingBox;
        this.isTriggerHitbox = isTriggerHitbox;
    }

    @Override
    public BoundingBox getHitbox() {
        return this.boundingBox;
    }

    @Override
    public boolean isTriggerHitbox() {
        return this.isTriggerHitbox;
    }

    @Override
    public BoundingBox getRenderHitbox(float partialTick) {
        return this.boundingBox;
    }
}
