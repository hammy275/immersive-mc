package net.blf02.immersivemc.client.immersive;

import net.blf02.immersivemc.client.immersive.info.AbstractTileEntityImmersiveInfo;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractTileEntityImmersive<T extends TileEntity, I extends AbstractTileEntityImmersiveInfo<T>>
    extends AbstractImmersive<I> {

    /**
     * Get a new instance of info to track.
     *
     * @param tileEnt Tile Entity that the info contains
     * @return The instance
     */
    public abstract I getNewInfo(T tileEnt);

    public abstract int getTickTime();

    public abstract boolean shouldRender(I info);

    // EVERYTHING ABOVE MUST BE OVERRIDEN, AND HAVE SUPER() CALLED IF APPLICABLE!

    public void trackObject(T tileEnt) {
        for (I info : getTrackedObjects()) {
            if (info.getTileEntity() == tileEnt) {
                info.setTicksLeft(getTickTime());
                return;
            }
        }
        infos.add(getNewInfo(tileEnt));
    }
}
