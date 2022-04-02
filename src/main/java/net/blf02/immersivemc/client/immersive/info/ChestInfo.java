package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class ChestInfo extends AbstractTileEntityImmersiveInfo<ChestTileEntity> {

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[54];
    public ChestTileEntity other = null;
    public Direction forward = null;

    public ChestInfo(ChestTileEntity tileEntity, int ticksToExist, ChestTileEntity other) {
        super(tileEntity, ticksToExist, 53); // Accounts for double chest
        this.other = other;
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
