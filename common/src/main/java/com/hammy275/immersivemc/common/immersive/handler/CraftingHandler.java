package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.server.storage.WorldStorage;
import com.hammy275.immersivemc.server.storage.WorldStorages;
import com.hammy275.immersivemc.server.storage.impl.CraftingTableWorldStorage;
import com.hammy275.immersivemc.server.storage.impl.ItemWorldStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;

public class CraftingHandler extends ItemWorldStorageHandlerImpl {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        CraftingTableWorldStorage worldStorage = (CraftingTableWorldStorage) WorldStorages.get(pos, player.serverLevel());
        return new ListOfItemsStorage(Arrays.asList(worldStorage.getItemsRaw()), 10);
    }

    @Override
    public HandlerStorage getEmptyHandlerStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        CraftingTableWorldStorage storage = (CraftingTableWorldStorage) WorldStorages.get(pos, player.serverLevel());
        if (slot < 9) {
            storage.placeItem(player, hand,
                    Swap.getPlaceAmount(player.getItemInHand(hand), mode),
                    slot);
            storage.setItem(9, Swap.getRecipeOutput(player, storage.getItemsRaw()));
        } else {
            Swap.handleDoCraft(player, storage.getItemsRaw(), pos);
            for (int i = 0; i <= 8; i++) {
                if (!storage.getItem(i).isEmpty()) {
                    storage.shrinkCountsOnly(i, 1);
                }
            }
        }
        storage.setDirty(player.serverLevel());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        // Can't do an instanceof CraftingTableBlock check here, since smithing tables and fletching tables
        // are both subclasses of that.
        return level.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE &&
                level.getBlockEntity(pos) == null; // Don't stand in the way of mods that store data in-table.
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useCraftingImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "crafting_table");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new CraftingTableWorldStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return CraftingTableWorldStorage.class;
    }

    @Override
    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemWorldStorage storage) {
        ItemStack out = Swap.getRecipeOutput(player, storage.getItemsRaw());
        storage.setItem(9, out);
    }
}
