package net.blf02.immersivemc.client.immersive;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.blf02.immersivemc.common.config.CommonConstants;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.FetchInventoryPacket;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AbstractWorldStorageImmersive<I extends AbstractWorldStorageInfo> extends AbstractImmersive<I> {
    public AbstractWorldStorageImmersive(int maxImmersives) {
        super(maxImmersives);
        Immersives.WS_IMMERSIVES.add(this);
    }

    public abstract void processStorageFromNetwork(AbstractWorldStorageInfo info, ImmersiveStorage storage);

    public abstract I getNewInfo(BlockPos pos);

    public abstract int getTickTime();

    @Override
    protected boolean slotShouldRenderHelpHitbox(I info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    protected void doTick(I info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (info.ticksActive % ClientConstants.inventorySyncTime == 0) {
            Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
        }
        if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.distanceToSqr(Vector3d.atCenterOf(info.getBlockPosition())) >
                        CommonConstants.distanceSquaredToRemoveImmersive) {
            info.remove();
        }
    }

    @Override
    public boolean shouldRender(I info, boolean isInVR) {
        return info.readyToRender();
    }

    public void trackObject(BlockPos pos) {
        for (I info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(getTickTime());
                return;
            }
        }
        infos.add(getNewInfo(pos));
    }
}
