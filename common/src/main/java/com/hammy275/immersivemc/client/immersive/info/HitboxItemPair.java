package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.ClientUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HitboxItemPair implements HitboxInfo {

    public @Nullable BoundingBox box;
    public ItemStack item;
    public boolean isTriggerHitbox;
    public @Nullable Vec3 lastPos = null;

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

    @Override
    public BoundingBox getRenderHitbox(float partialTick) {
        if (this.box == null || lastPos == null) {
            return this.box;
        }
        return BoundingBox.move(this.box, ClientUtil.lerpVec3(this.lastPos, BoundingBox.getCenter(this.box), partialTick).subtract(this.lastPos));
    }
}
