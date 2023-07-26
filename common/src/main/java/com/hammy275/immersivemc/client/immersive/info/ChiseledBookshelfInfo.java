package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookshelfInfo extends AbstractImmersiveInfo {

    protected Vec3[] booksPositions = new Vec3[6];
    protected AABB[] bookHitboxes = new AABB[6];
    protected BlockPos immersivePos;

    public ChiseledBookshelfInfo(BlockPos pos) {
        super(ClientConstants.ticksToRenderChiseledBookshelf);
        this.immersivePos = pos;
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = this.bookHitboxes;
    }

    @Override
    public AABB getHitbox(int slot) {
        return this.bookHitboxes[slot];
    }

    @Override
    public AABB[] getAllHitboxes() {
        return this.bookHitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        this.bookHitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return this.bookHitboxes[5] != null;
    }

    @Override
    public Vec3 getPosition(int slot) {
        return this.booksPositions[slot];
    }

    @Override
    public Vec3[] getAllPositions() {
        return this.booksPositions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        this.booksPositions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return this.booksPositions[5] != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasHitboxes() && this.hasPositions() && this.immersivePos != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.immersivePos;
    }
}
