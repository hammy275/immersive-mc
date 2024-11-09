package com.hammy275.immersivemc.common.compat.apotheosis;

import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface ApothCompat {

    public ApothStats getStats(Level level, BlockPos pos, int itemEnchantability);

    public ItemStack doEnchant(Player player, BlockPos pos, int tier, ItemStack item);

    public ETableStorage.SlotData[] getEnchData(Player player, BlockPos pos, ItemStack item);

    public boolean enchantModuleEnabled();

    public boolean suppressVanillaEnchanting();

    public boolean isSalvagingTable(Level level, BlockPos pos);

    public List<ItemStack> doSalvage(Player player, List<ItemStack> input, BlockPos pos);

    public boolean isSalvagable(ItemStack input, Level level);
}
