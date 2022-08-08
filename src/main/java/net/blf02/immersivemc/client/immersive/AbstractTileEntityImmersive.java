package net.blf02.immersivemc.client.immersive;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractBlockEntityImmersiveInfo;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.world.phys.Vec3;

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
    public abstract I getNewInfo(T tileEnt);

    public abstract int getTickTime();

    public abstract boolean shouldRender(I info, boolean isInVR);

    @Override
    protected boolean slotShouldRenderHelpHitbox(I info, int slotNum) {
        if (info.getBlockEntity() instanceof IInventory) {
            return (info.items[slotNum] == null || info.items[slotNum].isEmpty())
                    && info.getInputSlots()[slotNum] != null; // So far, only the chest can have a null input slot
        } else {
            // Should be implemented on sub-class
            throw new IllegalArgumentException("Can't check input slot has item for non-IInventory's!");
        }
    }

    @Override
    protected void doTick(I info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (info.getBlockEntity() instanceof IInventory) {
            if (info.ticksActive % ClientConstants.inventorySyncTime == 0) {
                Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
            }
        }

        if (Minecraft.getInstance().player != null && info.getBlockEntity() != null &&
                Minecraft.getInstance().player.distanceToSqr(Vec3.atCenterOf(info.getBlockEntity().getBlockPos())) >
                CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }

    }

    // EVERYTHING ABOVE MUST BE OVERRIDEN, AND HAVE SUPER() CALLED IF APPLICABLE!

    /**
     * Can be overriden as a final check before tracking an object.
     * @param tileEnt Tile entity to check one last time before possibly tracking
     * @return Whether or not we should track this
     */
    public boolean shouldTrack(T tileEnt) {
        return true;
    }

    public void trackObject(T tileEnt) {
        for (I info : getTrackedObjects()) {
            if (info.getBlockEntity() == tileEnt) {
                info.setTicksLeft(getTickTime());
                return;
            }
        }
        if (shouldTrack(tileEnt)) infos.add(getNewInfo(tileEnt));
    }
}
