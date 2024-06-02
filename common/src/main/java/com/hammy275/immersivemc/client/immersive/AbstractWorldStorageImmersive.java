package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class AbstractWorldStorageImmersive<I extends AbstractWorldStorageInfo, S extends NetworkStorage>
        extends AbstractImmersive<I, S> {
    public AbstractWorldStorageImmersive(int maxImmersives) {
        super(maxImmersives);
    }

    public abstract I getNewInfo(BlockPos pos);

    public abstract int getTickTime();

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(I info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    public boolean shouldRender(I info, boolean isInVR) {
        return info.readyToRender();
    }

    public I refreshOrTrackObject(BlockPos pos, Level level) {
        for (I info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(getTickTime());
                return info;
            }
        }
        I newInfo = getNewInfo(pos);
        infos.add(newInfo);
        return newInfo;
    }
}
