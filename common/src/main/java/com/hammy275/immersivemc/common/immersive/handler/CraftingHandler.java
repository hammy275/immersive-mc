package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.CraftingTableStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SmithingTableBlock;

public class CraftingHandler extends ItemWorldStorageHandler<CraftingTableStorage> {
    @Override
    public CraftingTableStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (CraftingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
    }

    @Override
    public CraftingTableStorage getEmptyNetworkStorage() {
        return new CraftingTableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        CraftingTableStorage storage = (CraftingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        if (slot < 9) {
            storage.placeItem(player, hand, slot, amount);
            storage.setItem(9, Swap.getRecipeOutput(player, storage.getItemsRaw()));
        } else {
            ItemStack[] newSlots = Swap.handleDoCraft(player, storage.getItemsRaw(), pos, amount);
            if (newSlots == null) return;
            for (int i = 0; i <= 8; i++) {
                ItemStack storageItem = storage.getItem(i);
                if (!storageItem.isEmpty()) {
                    if (Util.stacksEqualBesidesCount(storageItem, newSlots[i])) {
                        int diff = storageItem.getCount() - newSlots[i].getCount();
                        storage.shrinkSlot(i, diff);
                    } else {
                        storage.setItem(i, newSlots[i], player);
                    }
                }
            }
            storage.setItem(9, newSlots[9]);
        }
        storage.setDirty(player.level());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        Block block = level.getBlockState(pos).getBlock();
        return isCraftingTableBlock(block) && level.getBlockEntity(pos) == null; // Don't stand in the way of mods that store data in-table.
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useCraftingTableImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("crafting_table");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new CraftingTableStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return CraftingTableStorage.class;
    }

    @Override
    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storage) {
        ItemStack out = Swap.getRecipeOutput(player, storage.getItemsRaw());
        storage.setItem(9, out);
    }

    public static boolean isCraftingTableBlock(Block block) {
        return block instanceof CraftingTableBlock && !(block instanceof SmithingTableBlock);
    }
}
