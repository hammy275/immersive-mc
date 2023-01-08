package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChestInfo extends AbstractBlockEntityImmersiveInfo<BlockEntity> {

    protected AABB[] hitboxes = new AABB[54];
    public BlockEntity other = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;
    public boolean isOpen = false;
    public double lastY0;
    public double lastY1;
    public AABB[] openCloseHitboxes = new AABB[]{null, null};
    public Vec3[] openClosePositions = new Vec3[]{null, null};
    public int openCloseCooldown = 0;
    public boolean isTFCChest;

    public ChestInfo(BlockEntity tileEntity, int ticksToExist, BlockEntity other) {
        super(tileEntity, ticksToExist, 53); // Accounts for double chest
        this.other = other;
        this.isTFCChest = tileEntity.getClass().getName().startsWith("net.dries007.tfc");
    }

    public void nextRow() {
        rowNum = getNextRow(rowNum);
    }

    public int getNextRow(int rowIn) {
        int rowMax = this.isTFCChest ? 1 : 2;
        if (++rowIn > rowMax) {
            return 0;
        }
        return rowIn;
    }

    public int getRowNum() {
        return rowNum;
    }

    @Override
    public void setInputSlots() {
        if (this.isOpen) {
            this.inputHitboxes = this.hitboxes;
        } else {
            this.inputHitboxes = new AABB[0];
        }

    }

    @Override
    public AABB getHitbox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public AABB[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return (hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null);
    }

    @Override
    public boolean hasItems() {
        boolean mainChest;
        boolean otherChest;
        if (this.isTFCChest) {
            mainChest = items[17] != null;
            otherChest = this.other == null || items[44] != null;
        } else {
            mainChest = items[26] != null;
            otherChest = this.other == null || items[53] != null;
        }
        return mainChest && otherChest;
    }
}
