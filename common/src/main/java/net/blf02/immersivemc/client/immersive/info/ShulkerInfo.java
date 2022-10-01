package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;

public class ShulkerInfo extends AbstractBlockEntityImmersiveInfo<ShulkerBoxBlockEntity> {

    protected AABB[] hitboxes = new AABB[27];
    public boolean isOpen = false;
    protected int rowNum = 0;
    public Direction lastDir = null;

    /**
     * Constructor
     *
     * @param tileEntity   Tile entity for immersion
     * @param ticksToExist Ticks this immersion should exist for after being hovered
     */
    public ShulkerInfo(ShulkerBoxBlockEntity tileEntity, int ticksToExist) {
        super(tileEntity, ticksToExist, 26);
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
        return this.hitboxes[slot];
    }

    @Override
    public AABB[] getAllHitboxes() {
        return this.hitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        this.hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null;
    }

    public void nextRow() {
        rowNum = getNextRow(rowNum);
    }

    public int getNextRow(int rowIn) {
        if (++rowIn > 2) {
            return 0;
        }
        return rowIn;
    }

    @Override
    public boolean readyToRender() {
        return super.readyToRender();
    }

    @Override
    public boolean hasPositions() {
        return positions[8] != null || positions[17] != null || positions[26] != null;
    }

    public int getRowNum() {
        return rowNum;
    }
}
