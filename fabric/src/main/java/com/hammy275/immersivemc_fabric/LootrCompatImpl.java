package com.hammy275.immersivemc_fabric;

import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.zestyblaze.lootr.api.LootrAPI;
import net.zestyblaze.lootr.api.blockentity.ILootBlockEntity;
import net.zestyblaze.lootr.api.inventory.ILootrInventory;
import org.jetbrains.annotations.Nullable;

public class LootrCompatImpl implements LootrCompat {
    @Override
    public @Nullable Container getContainer(ServerPlayer player, BlockPos pos) {
        Level level = player.level;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ILootBlockEntity lootBE &&
                blockEntity instanceof RandomizableContainerBlockEntity rcbe) {
            MenuProvider provider = LootrAPI.getModdedMenu(level, lootBE.getTileId(), pos,
                    player, rcbe, lootBE::unpackLootTable, lootBE::getTable,
                    lootBE::getSeed);
            if (provider instanceof ILootrInventory inventory) {
                return inventory;
            }
        }
        return null;
    }

    @Override
    public void markOpener(Player player, BlockPos pos) {
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof ILootBlockEntity lootBE) {
            if (player.level.isClientSide) {
                lootBE.setOpened(true);
            } else {
                lootBE.getOpeners().add(player.getUUID());
                blockEntity.setChanged();
            }
        }
    }
}
