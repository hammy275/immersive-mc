package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.AABB;

public class BarrelInfo extends AbstractBlockEntityImmersiveInfo<BarrelBlockEntity> {

    public int rowNum = 0;
    public boolean isOpen = false;
    protected AABB[] hitboxes = new AABB[27];
    public boolean updateHitboxes = false;
    public Direction forward = null;

    public BarrelInfo(BarrelBlockEntity tileEntity) {
        super(tileEntity, ClientConstants.ticksToRenderBarrel, 26);
    }

    public void nextRow() {
        rowNum = getNextRow(rowNum);
        updateHitboxes = true;
    }

    public int getNextRow(int rowIn) {
        if (++rowIn > 2) {
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
            this.inputHitboxes = hitboxes;
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
    public boolean readyToRender() {
        return super.readyToRender() && forward != null;
    }

    @Override
    public boolean hasPositions() {
        return positions[8] != null || positions[17] != null || positions[26] != null;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null;
    }
}
