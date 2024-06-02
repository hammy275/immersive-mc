package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.RelativeHitboxInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for infos that Immersives constructed with the builder can use.
 */
public final class BuiltImmersiveInfoImpl<E> implements BuiltImmersiveInfo<E> {

    public final List<RelativeHitboxInfo> hitboxes;
    public final List<HitboxInfo> hitboxesOut;
    // Key is input slot number, value is a HitboxInfo
    private BlockPos pos;
    public final E extraData;
    public Direction immersiveDir = null; // Stores the direction the immersive is facing (towards the player, for example)
    public boolean airCheckPassed = false;
    public int[] slotsHovered = new int[]{-1, -1};
    public int light = AbstractImmersive.maxLight;
    public long ticksExisted = 0;

    public BuiltImmersiveInfoImpl(List<RelativeHitboxInfo> hitboxes, BlockPos pos, Class<E> extraDataClazz) {
        this.hitboxes = new ArrayList<>(hitboxes.size());
        this.hitboxesOut = new ArrayList<>(hitboxes.size());
        for (RelativeHitboxInfo hitbox : hitboxes) {
            RelativeHitboxInfo clone = (RelativeHitboxInfo) hitbox.clone();
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
        for (RelativeHitboxInfo hitbox : this.hitboxes) {
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
    public boolean isSlotHovered(int hitboxIndex) {
        return slotsHovered[0] == hitboxIndex || slotsHovered[1] == hitboxIndex;
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
