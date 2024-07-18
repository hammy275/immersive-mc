package com.hammy275.immersivemc.common.api_impl.hitbox;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;

public class HitboxInfoFactoryImpl implements HitboxInfoFactory {

    public static final HitboxInfoFactory INSTANCE = new HitboxInfoFactoryImpl();

    @Override
    public HitboxInfo interactHitbox(BoundingBox boundingBox) {
        return new HitboxInfoImpl(boundingBox, false);
    }

    @Override
    public HitboxInfo triggerHitbox(BoundingBox boundingBox) {
        return new HitboxInfoImpl(boundingBox, true);
    }
}
