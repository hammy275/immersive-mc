package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class ChestInfo extends AbstractTileEntityImmersiveInfo<TileEntity> {

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[54];
    public TileEntity other = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;
    public boolean isOpen = false;

    public ChestInfo(TileEntity tileEntity, int ticksToExist, TileEntity other) {
        super(tileEntity, ticksToExist, 53); // Accounts for double chest
        this.other = other;
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

    public int getRowNum() {
        return rowNum;
    }

    @Override
    public AxisAlignedBB getHibtox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return (hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null)
                && isOpen;
    }

    @Override
    public boolean hasItems() {
        boolean mainChest = items[26] != null;
        boolean otherChest = this.other == null || items[53] != null;
        return mainChest && otherChest;
    }
}
