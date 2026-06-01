package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class ItemWorldStorageHandler<S extends NetworkStorage> implements WorldStorageHandler<S> {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos) {
        if (WorldStoragesImpl.getS(pos, tracker.level()) instanceof ItemStorage iws) {
            return iws.isDirtyForClientSync();
        }
        return false;
    }

    @Override
    public void onStopTracking(ServerPlayer tracker, BlockPos pos) {
        if (WorldStoragesImpl.getWithoutVerificationS(pos, tracker.level()) instanceof ItemStorage iws) {
            if (Util.isValidBlocks(this, pos, tracker.level())) {
                if (ActiveConfig.getConfigForPlayer(tracker).returnItemsWhenLeavingImmersives) { // Player left block range
                    iws.returnItems(tracker);
                    updateStorageOutputAfterItemReturn(tracker, pos, iws);
                    iws.setDirty(tracker.level());
                }
            } else if (tracker.level().getBlockState(pos).isAir()) {
                // Block was destroyed. Need to air check above, since getting block entities returns null when paused
                // such as with the config screen.
                for (int i = 0; i <= iws.maxInputIndex; i++) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ItemStack stack = iws.getItem(i);
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(tracker.level(), vecPos.x, vecPos.y, vecPos.z, stack);
                        tracker.level().addFreshEntity(itemEnt);
                    }
                }
                WorldStoragesImpl.removeS(pos, tracker.level());
            }
        }
    }

    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storage) {}
}
