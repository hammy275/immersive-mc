package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.TileEntity;

public abstract class AbstractTileEntityImmersiveInfo<T extends TileEntity> extends AbstractImmersiveInfo {

    protected T tileEntity;

    public AbstractTileEntityImmersiveInfo(T tileEntity, int ticksToExist) {
        super(ticksToExist);
        this.tileEntity = tileEntity;
    }

    public T getTileEntity() {
        return this.tileEntity;
    }
}
