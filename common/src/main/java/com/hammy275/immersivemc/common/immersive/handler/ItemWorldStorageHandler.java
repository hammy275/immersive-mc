package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class ItemWorldStorageHandler implements WorldStorageHandler {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        if (WorldStorages.get(pos, player.getLevel()) instanceof ItemStorage iws) {
            return iws.isDirtyForClientSync();
        }
        return false;
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        if (WorldStorages.get(pos, player.getLevel()) instanceof ItemStorage iws) {
            iws.setNoLongerDirtyForClientSync();
        }
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        if (WorldStorages.getWithoutVerification(pos, player.getLevel()) instanceof ItemStorage iws) {
            if (isValidBlock(pos, player.level)) {
                if (ActiveConfig.getConfigForPlayer(player).returnItems) { // Player left block range
                    iws.returnItems(player);
                    updateStorageOutputAfterItemReturn(player, pos, iws);
                    iws.setDirty(player.getLevel());
                }
            } else if (player.level.getBlockState(pos).isAir()) {
                // Block was destroyed. Need to air check above, since getting block entities returns null when paused
                // such as with the config screen.
                for (int i = 0; i <= iws.maxInputIndex; i++) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ItemStack stack = iws.getItem(i);
                    if (stack != null && !stack.isEmpty()) {
                        ItemEntity itemEnt = new ItemEntity(player.level, vecPos.x, vecPos.y, vecPos.z, stack);
                        player.level.addFreshEntity(itemEnt);
                    }
                }
                WorldStorages.remove(pos, player.getLevel());
            }
        }
    }

    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storage) {}
}
