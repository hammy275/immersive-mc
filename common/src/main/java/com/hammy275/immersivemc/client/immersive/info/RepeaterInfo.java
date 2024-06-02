package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.api.common.obb.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RepeaterInfo extends AbstractImmersiveInfo {

    protected BoundingBox[] hitboxes = new BoundingBox[4];
    protected Vec3[] positions = new Vec3[4];
    protected final BlockPos pos;
    public boolean[] grabbedCurrent = new boolean[]{false, false}; // Index is for controller num

    public RepeaterInfo(BlockPos pos) {
        super(ClientConstants.ticksToRenderRepeater);
        this.pos = pos;
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = hitboxes;
    }

    @Override
    public BoundingBox getHitbox(int index) {
        return hitboxes[index];
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes[3] != null;
    }

    @Override
    public Vec3 getPosition(int index) {
        return positions[index];
    }

    @Override
    public Vec3[] getAllPositions() {
        return positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return positions[3] != null;
    }

    @Override
    public boolean readyToRender() {
        return hasHitboxes() && hasPositions();
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }
}
