package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.RelativeHitboxInfoImpl;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for infos that Immersives constructed with the builder can use.
 */
public final class BuiltImmersiveInfoImpl<E> implements BuiltImmersiveInfo<E> {

    public final List<RelativeHitboxInfoImpl> hitboxes;
    public final List<HitboxInfo> hitboxesOut;
    // Key is input slot number, value is a HitboxInfo
    private final BlockPos pos;
    public final E extraData;
    public Direction immersiveDir = null; // Stores the direction the immersive is facing (towards the player, for example)
    public boolean airCheckPassed = false;
    public int[] slotsHovered = new int[]{-1, -1};
    public int light = ClientUtil.maxLight;
    public long ticksExisted = 0;
    public AABB dragHitbox = null;

    public BuiltImmersiveInfoImpl(List<RelativeHitboxInfoImpl> hitboxes, BlockPos pos, Class<E> extraDataClazz) {
        this.hitboxes = new ArrayList<>(hitboxes.size());
        this.hitboxesOut = new ArrayList<>(hitboxes.size());
        for (RelativeHitboxInfoImpl hitbox : hitboxes) {
            RelativeHitboxInfoImpl clone = (RelativeHitboxInfoImpl) hitbox.clone();
            this.hitboxes.add(clone);
            this.hitboxesOut.add(clone);
        }
        this.pos = pos;
        try {
            this.extraData = extraDataClazz == null ? null : extraDataClazz.getDeclaredConstructor(null).newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<HitboxInfo> getAllHitboxes() {
        return hitboxesOut;
    }

    @Override
    public boolean hasHitboxes() {
        // Only need to have one hitbox to be valid.
        for (RelativeHitboxInfoImpl hitbox : this.hitboxes) {
            if (hitbox.hasAABB()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.pos;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        slotsHovered[handIndex] = hitboxIndex;
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return slotsHovered[handIndex];
    }

    @Override
    public boolean isSlotHovered(int hitboxIndex) {
        return slotsHovered[0] == hitboxIndex || slotsHovered[1] == hitboxIndex || SwapTracker.slotHovered(this, hitboxIndex);
    }

    @Override
    public long getTicksExisted() {
        return ticksExisted;
    }

    public E getExtraData() {
        return this.extraData;
    }

    @Override
    public ItemStack getItem(int hitboxIndex) {
        ItemStack item = this.hitboxes.get(hitboxIndex).item;
        return item == null ? ItemStack.EMPTY : item;
    }

    @Override
    public void setFakeItem(int hitboxIndex, ItemStack item) {
        this.hitboxes.get(hitboxIndex).item = item;
    }

    @Override
    public long ticksExisted() {
        return this.ticksExisted;
    }
}
