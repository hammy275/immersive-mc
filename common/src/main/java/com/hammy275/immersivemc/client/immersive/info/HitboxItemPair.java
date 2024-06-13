package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HitboxItemPair implements HitboxInfo {

    public @Nullable BoundingBox box;
    public ItemStack item;
    public boolean isTriggerHitbox;

    public HitboxItemPair(@Nullable BoundingBox box, ItemStack item, boolean isTriggerHitbox) {
        this.box = box;
        this.item = Objects.requireNonNull(item);
        this.isTriggerHitbox = isTriggerHitbox;
    }

    public HitboxItemPair(@Nullable BoundingBox box, boolean isTriggerHitbox) {
        this(box, ItemStack.EMPTY, isTriggerHitbox);
    }

    @Override
    @Nullable
    public BoundingBox getHitbox() {
        return box;
    }

    @Override
    public boolean isTriggerHitbox() {
        return isTriggerHitbox;
    }
}
