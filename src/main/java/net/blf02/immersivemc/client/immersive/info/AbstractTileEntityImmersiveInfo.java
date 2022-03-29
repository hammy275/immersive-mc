package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AbstractTileEntityImmersiveInfo<T extends TileEntity> extends AbstractImmersiveInfo {

    protected T tileEntity;
    protected Vector3d[] positions;

    public AbstractTileEntityImmersiveInfo(T tileEntity, int ticksToExist, int maxSlotIndex) {
        super(ticksToExist);
        this.tileEntity = tileEntity;
        this.positions = new Vector3d[maxSlotIndex+1];
    }

    public T getTileEntity() {
        return this.tileEntity;
    }

    @Override
    public Vector3d getPosition(int slot) {
        return positions[slot];
    }

    @Override
    public Vector3d[] getAllPositions() {
        return positions;
    }

    @Override
    public void setPosition(int slot, Vector3d position) {
        positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return positions[0] != null;
    }
}
