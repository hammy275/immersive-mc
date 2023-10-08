package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractWorldStorageImmersive<I extends AbstractWorldStorageInfo> extends AbstractImmersive<I> {
    public AbstractWorldStorageImmersive(int maxImmersives) {
        super(maxImmersives);
        Immersives.WS_IMMERSIVES.add(this);
    }

    public abstract void processStorageFromNetwork(AbstractWorldStorageInfo info, ImmersiveStorage storage);

    public abstract I getNewInfo(BlockPos pos);

    public abstract int getTickTime();

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(I info, int slotNum) {
        return info.items[slotNum] == null || info.items[slotNum].isEmpty();
    }

    @Override
    protected void doTick(I info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (info.ticksActive % ClientConstants.inventorySyncTime == 0) {
            Network.INSTANCE.sendToServer(new FetchInventoryPacket(info.getBlockPosition()));
        }
    }

    @Override
    public boolean shouldRender(I info, boolean isInVR) {
        return info.readyToRender();
    }

    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (I info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(getTickTime());
                return;
            }
        }
        infos.add(getNewInfo(pos));
    }
}
