package com.hammy275.immersivemc.common.compat.apotheosis;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ApothNullImpl extends ApothBaseCompatImpl {
    @Override
    public ApothStats getStats(Level level, BlockPos pos, int itemEnchantability) {
        return ApothStats.EMPTY;
    }

    @Override
    public ItemStack doEnchant(Player player, BlockPos pos, int tier, ItemStack item) {
        return item;
    }

    @Override
    public ETableStorage.SlotData[] getEnchData(Player player, BlockPos pos, ItemStack item) {
        return new ETableStorage.SlotData[]{ETableStorage.SlotData.DEFAULT, ETableStorage.SlotData.DEFAULT, ETableStorage.SlotData.DEFAULT};
    }

    @Override
    public boolean enchantModuleEnabled() {
        return false;
    }

    @Override
    public boolean isSalvagingTable(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public List<ItemStack> doSalvage(Player player, List<ItemStack> input, BlockPos pos) {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            out.add(ItemStack.EMPTY);
        }
        return out;
    }

    @Override
    public boolean isSalvagable(ItemStack input, Level level) {
        return false;
    }
}
