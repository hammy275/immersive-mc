package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.server.storage.WorldStorage;
import com.hammy275.immersivemc.server.storage.WorldStorages;
import com.hammy275.immersivemc.server.storage.impl.ItemWorldStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class ItemWorldStorageHandlerImpl implements WorldStorageHandler {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return ((ItemWorldStorage) WorldStorages.get(pos, player.serverLevel())).isDirtyForClientSync();
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        WorldStorage storage = WorldStorages.get(pos, player.serverLevel());
        if (storage instanceof ItemWorldStorage iws) {
            iws.setNoLongerDirtyForClientSync();
        }
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        WorldStorage worldStorage = WorldStorages.get(pos, player.serverLevel());
        if (worldStorage instanceof ItemWorldStorage iws) {
            if (isValidBlock(pos, player.level())) {
                if (ActiveConfig.getConfigForPlayer(player).returnItems) { // Player left block range
                    iws.returnItems(player);
                    updateStorageOutputAfterItemReturn(player, pos, iws);
                    iws.setDirty(player.serverLevel());
                }
            } else if (player.level().getBlockState(pos).isAir()) {
                // Block was destroyed. Need to air check above, since getting block entities returns null when paused
                // such as with the config screen.
                for (int i = 0; i <= iws.maxInputIndex; i++) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ItemStack stack = iws.getItem(i);
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(player.level(), vecPos.x, vecPos.y, vecPos.z, stack);
                        player.level().addFreshEntity(itemEnt);
                    }
                }
                WorldStorages.remove(pos, player.serverLevel());
            }
        }
    }

    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemWorldStorage storage) {}
}
