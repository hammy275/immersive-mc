package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.obb.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractWorldStorageInfo extends AbstractImmersiveInfo {

    protected Vec3[] positions;
    protected BoundingBox[] hitboxes;
    public ItemStack[] items;
    protected final BlockPos pos;
    public final int maxSlotIndex;

    public AbstractWorldStorageInfo(BlockPos pos, int ticksToExist, int maxSlotIndex) {
        super(ticksToExist);
        this.pos = pos;
        this.positions = new Vec3[maxSlotIndex + 1];
        this.items = new ItemStack[maxSlotIndex + 1];
        this.hitboxes = new BoundingBox[maxSlotIndex + 1];
        this.maxSlotIndex = maxSlotIndex;
    }

    @Override
    public BoundingBox getHitbox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return this.hitboxes;
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        this.hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return this.hitboxes[maxSlotIndex] != null;
    }

    @Override
    public Vec3 getPosition(int slot) {
        return this.positions[slot];
    }

    @Override
    public Vec3[] getAllPositions() {
        return this.positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        this.positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return this.positions[maxSlotIndex] != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasHitboxes() && this.hasPositions() && this.items[maxSlotIndex] != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.pos;
    }
}
