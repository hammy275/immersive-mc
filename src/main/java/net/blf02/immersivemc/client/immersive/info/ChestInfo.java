package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class ChestInfo extends AbstractTileEntityImmersiveInfo<TileEntity> {

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[54];
    public TileEntity other = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;
    public boolean isOpen = false;
    public double lastY0;
    public double lastY1;
    public AxisAlignedBB[] openCloseHitboxes = new AxisAlignedBB[]{null, null};
    public Vector3d[] openClosePositions = new Vector3d[]{null, null};
    public int openCloseCooldown = 0;

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
    public void setInputSlots() {
        if (this.isOpen) {
            int size = other == null ? 9 : 18;
            AxisAlignedBB[] inputs = new AxisAlignedBB[size];
            int i = 0;
            for (AxisAlignedBB aabb : hitboxes) {
                if (aabb != null) {
                    inputs[i++] = aabb;
                }
            }
            this.inputHitboxes = inputs;
        } else {
            this.inputHitboxes = new AxisAlignedBB[0];
        }

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
        return (hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null);
    }

    @Override
    public boolean hasItems() {
        boolean mainChest = items[26] != null;
        boolean otherChest = this.other == null || items[53] != null;
        return mainChest && otherChest;
    }
}
