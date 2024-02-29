package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class CraftingHandler extends WorldStorageHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage immersiveStorage = GetStorage.getCraftingStorage(player, pos);
        return new ListOfItemsStorage(Arrays.asList(immersiveStorage.getItemsRaw()), 10);
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        ImmersiveStorage storage = GetStorage.getCraftingStorage(player, pos);
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
        storage.setDirty();
    }

    @Override
    public boolean usesWorldStorage() {
        return true;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE;
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
    public ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos) {
        return GetStorage.getCraftingStorage(player, pos);
    }
}
