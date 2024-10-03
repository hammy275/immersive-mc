package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.mixin.ChestLidControllerAccessor;
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
import noobanidus.mods.lootr.api.LootrAPI;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.api.inventory.ILootrInventory;
import noobanidus.mods.lootr.block.entities.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.block.entities.LootrChestBlockEntity;
import noobanidus.mods.lootr.block.entities.LootrShulkerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class LootrCompatImpl implements LootrCompat {

    private static final Field chestChestLidController;

    static {
        Field chestLidControllerField = null;
        try {
            chestLidControllerField = LootrChestBlockEntity.class.getDeclaredField("chestLidController");
            chestLidControllerField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
        chestChestLidController = chestLidControllerField;
    }

    private LootrCompatImpl() {}

    public static LootrCompat makeCompatImpl() {
        return CompatModule.create(new LootrCompatImpl(), LootrCompat.class, Lootr.compatData);
    }

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

    @Override
    public boolean isLootrContainer(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(LootrCompat.BLOCK_TAG);
    }

    @Override
    public boolean openLootrBarrel(BlockPos pos, Player player, boolean nowOpen) {
        if (player.level.getBlockEntity(pos) instanceof LootrBarrelBlockEntity lbbe) {
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
        if (player.level.getBlockEntity(pos) instanceof LootrShulkerBlockEntity lsbe) {
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
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof LootrShulkerBlockEntity lsbe) {
            return !lsbe.isClosed();
        } else if (be instanceof LootrBarrelBlockEntity) {
            return player.level.getBlockState(pos).getValue(BarrelBlock.OPEN);
        } else if (be instanceof LootrChestBlockEntity) {
            try {
                return ((ChestLidControllerAccessor) chestChestLidController.get(be)).getShouldBeOpen();
            } catch (IllegalAccessException ignored) {}
        }
        return false;
    }

}
