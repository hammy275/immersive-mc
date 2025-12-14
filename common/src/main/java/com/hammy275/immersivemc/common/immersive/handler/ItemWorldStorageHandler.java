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
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        if (WorldStoragesImpl.getS(pos, player.level()) instanceof ItemStorage iws) {
            return iws.isDirtyForClientSync();
        }
        return false;
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        if (WorldStoragesImpl.getWithoutVerificationS(pos, player.level()) instanceof ItemStorage iws) {
            if (Util.isValidBlocks(this, pos, player.level())) {
                if (ActiveConfig.getConfigForPlayer(player).returnItemsWhenLeavingImmersives) { // Player left block range
                    iws.returnItems(player);
                    updateStorageOutputAfterItemReturn(player, pos, iws);
                    iws.setDirty(player.level());
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
                WorldStoragesImpl.removeS(pos, player.level());
            }
        }
    }

    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storage) {}
}
