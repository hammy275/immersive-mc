package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.AbstractBlockEntityImmersiveInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBlockEntityImmersive<T extends BlockEntity, I extends AbstractBlockEntityImmersiveInfo<T>>
    extends AbstractImmersive<I> {

    public AbstractBlockEntityImmersive(int maxImmersives) {
        super(maxImmersives);
    }

    /**
     * Get a new instance of info to track.
     *
     * @param tileEnt Tile Entity that the info contains
     * @return The instance
     */
    public abstract I getNewInfo(BlockEntity tileEnt);

    public abstract int getTickTime();

    public abstract boolean shouldRender(I info, boolean isInVR);

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(I info, int slotNum) {
        if (info.getBlockEntity() instanceof Container) {
            return (info.items[slotNum] == null || info.items[slotNum].isEmpty())
                    && info.getInputSlots()[slotNum] != null; // So far, only the chest can have a null input slot
        } else {
            // Should be implemented on sub-class
            throw new IllegalArgumentException("Can't check input slot has item for non-Container's!");
        }
    }

    // EVERYTHING ABOVE MUST BE OVERRIDEN, AND HAVE SUPER() CALLED IF APPLICABLE!

    /**
     * Can be overriden as a final check before tracking an object.
     * @param tileEnt Tile entity to check one last time before possibly tracking
     * @return Whether or not we should track this
     */
    public boolean reallyShouldTrack(BlockEntity tileEnt) {
        return true;
    }

    public I refreshOrTrackObject(BlockPos pos, BlockState state, BlockEntity tileEnt, Level level) {
        for (I info : getTrackedObjects()) {
            if (info.getBlockEntity() == tileEnt) {
                info.setTicksLeft(getTickTime());
                return info;
            }
        }
        if (reallyShouldTrack(tileEnt)) {
            I newInfo = getNewInfo(tileEnt);
            infos.add(newInfo);
            return newInfo;
        }
        return null;
    }
}
