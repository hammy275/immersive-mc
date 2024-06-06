package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class HitboxItemPair implements HitboxInfo {

    public BoundingBox box;
    public ItemStack item;
    public boolean isTriggerHitbox;

    public HitboxItemPair(BoundingBox box, ItemStack item, boolean isTriggerHitbox) {
        this.box = box;
        this.item = Objects.requireNonNull(item);
        this.isTriggerHitbox = isTriggerHitbox;
    }

    @Override
    public BoundingBox getHitbox() {
        return box;
    }

    @Override
    public boolean isTriggerHitbox() {
        return isTriggerHitbox;
    }
}
