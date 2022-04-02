package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class ChestInfo extends AbstractTileEntityImmersiveInfo<ChestTileEntity> {

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[54];
    public ChestTileEntity other = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;

    public ChestInfo(ChestTileEntity tileEntity, int ticksToExist, ChestTileEntity other) {
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
        return hitboxes[26] != null;
    }
}
