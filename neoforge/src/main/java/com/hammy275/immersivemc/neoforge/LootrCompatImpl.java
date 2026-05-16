package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.compat.lootr.LootrCompat;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.data.ILootrData;
import noobanidus.mods.lootr.common.api.filler.DefaultLootFiller;
import noobanidus.mods.lootr.common.api.interfaces.IHasOpeners;
import noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrShulkerBoxBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LootrCompatImpl implements LootrCompat {

    private LootrCompatImpl() {}

    public static LootrCompat makeCompatImpl() {
        return CompatModule.create(new LootrCompatImpl(), LootrCompat.class, Lootr.compatData);
    }

    @Override
    public @Nullable Container getContainer(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        ILootrContainerInstance provider = ILootrContainerInstance.of(pos, level);
        if (provider == null) {
            return null;
        }
        return LootrAPI.getInventory(provider, player, DefaultLootFiller.getInstance(), null);
    }

    @Override
    public void markOpener(Player player, BlockPos pos) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof IHasOpeners lootrOpeners) {
            lootrOpeners.addOpener(player);
        }
    }

    @Override
    public boolean isLootrContainer(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(LootrCompat.BLOCK_TAG);
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
        if (player.level().getBlockEntity(pos) instanceof LootrShulkerBoxBlockEntity lsbe) {
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
        if (be instanceof ILootrData lootrInfo) {
            return lootrInfo.isPhysicallyOpen();
        }
        return false;
    }

}
