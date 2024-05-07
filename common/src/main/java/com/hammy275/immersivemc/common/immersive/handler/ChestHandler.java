package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ChestHandler extends ChestLikeHandler {

    @Override
    public NetworkStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            ListOfItemsStorage storage = (ListOfItemsStorage) super.makeInventoryContents(player, pos);
            ChestBlockEntity otherChest = Util.getOtherChest(cbe);
            if (otherChest != null) {
                ListOfItemsStorage otherStorage = (ListOfItemsStorage) super.makeInventoryContents(player, otherChest.getBlockPos());
                storage.getItems().addAll(otherStorage.getItems());
            }
            return storage;
        } else { // Is an ender chest
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
                items.add(player.getEnderChestInventory().getItem(i));
            }
            return new ListOfItemsStorage(items, 27);
        }
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            Swap.handleChest(cbe, player, hand, slot);
        } else if (blockEntity instanceof EnderChestBlockEntity) {
            Swap.handleEnderChest(player, hand, slot);
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        if (blockEntity instanceof EnderChestBlockEntity) {
            return player.tickCount % 2 == 0; // Every other tick for dirtiness. Not ideal, but works.
        } else {
            boolean isDirtyForClientSync = super.isDirtyForClientSync(player, pos);
            ChestBlockEntity otherChest = Util.getOtherChest((ChestBlockEntity) player.level.getBlockEntity(pos));
            if (otherChest != null) {
                isDirtyForClientSync = isDirtyForClientSync || super.isDirtyForClientSync(player, otherChest.getBlockPos());
            }
            return isDirtyForClientSync;
        }
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useChestImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "chest");
    }
}
