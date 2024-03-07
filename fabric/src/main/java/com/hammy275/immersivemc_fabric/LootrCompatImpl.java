package com.hammy275.immersivemc_fabric;

import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.zestyblaze.lootr.api.LootrAPI;
import net.zestyblaze.lootr.api.blockentity.ILootBlockEntity;
import net.zestyblaze.lootr.api.inventory.ILootrInventory;
import net.zestyblaze.lootr.blocks.entities.LootrBarrelBlockEntity;
import net.zestyblaze.lootr.blocks.entities.LootrChestBlockEntity;
import net.zestyblaze.lootr.blocks.entities.LootrShulkerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LootrCompatImpl implements LootrCompat {
    @Override
    public @Nullable Container getContainer(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
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
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof ILootBlockEntity lootBE) {
            if (player.level().isClientSide) {
                lootBE.setOpened(true);
            } else {
                lootBE.getOpeners().add(player.getUUID());
                blockEntity.setChanged();
            }
        }
    }

    @Override
    public boolean isLootrContainer(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof ILootBlockEntity;
    }

    @Override
    public boolean openLootrBarrel(BlockPos pos, Player player, boolean nowOpen) {
        if (player.level().getBlockEntity(pos) instanceof LootrBarrelBlockEntity lbbe) {
            if (nowOpen) {
                lbbe.startOpen(player);
                ChestToOpenSet.openChest(player, pos);
            } else {
                lbbe.stopOpen(player);
                ChestToOpenSet.closeChest(player, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean openLootrShulkerBox(BlockPos pos, Player player, boolean nowOpen) {
        if (player.level().getBlockEntity(pos) instanceof LootrShulkerBlockEntity lsbe) {
            if (nowOpen) {
                lsbe.startOpen(player);
                ChestToOpenSet.openChest(player, pos);
            } else {
                lsbe.stopOpen(player);
                ChestToOpenSet.closeChest(player, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isOpen(BlockPos pos, Player player) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof LootrShulkerBlockEntity lsbe) {
            return !lsbe.isClosed();
        } else if (be instanceof LootrBarrelBlockEntity) {
            return player.level().getBlockState(pos).getValue(BarrelBlock.OPEN);
        } else if (be instanceof LootrChestBlockEntity lcbe) {
            return lcbe.getOpenNess(1f) > 0;
        }
        return false;
    }

}
