package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class ChestHandler extends ChestLikeHandler {

    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ListOfItemsStorage storage = (ListOfItemsStorage) super.makeInventoryContents(player, pos);
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            ChestBlockEntity otherChest = Util.getOtherChest(cbe);
            if (otherChest != null) {
                ListOfItemsStorage otherStorage = (ListOfItemsStorage) super.makeInventoryContents(player, otherChest.getBlockPos());
                storage.getItems().addAll(otherStorage.getItems());
            }
        }
        return storage;
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            Swap.handleChest(cbe, player, hand, slot);
        } else if (blockEntity instanceof EnderChestBlockEntity) {
            Swap.handleEnderChest(player, hand, slot);
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        boolean isDirtyForClientSync = super.isDirtyForClientSync(player, pos);
        ChestBlockEntity otherChest = Util.getOtherChest((ChestBlockEntity) player.level().getBlockEntity(pos));
        if (otherChest != null) {
            isDirtyForClientSync = isDirtyForClientSync || super.isDirtyForClientSync(player, otherChest.getBlockPos());
        }
        return isDirtyForClientSync;
    }

    @Override
    public void clearDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        super.clearDirtyForClientSync(player, pos);
        ChestBlockEntity otherChest = Util.getOtherChest((ChestBlockEntity) player.level().getBlockEntity(pos));
        if (otherChest != null) {
            super.clearDirtyForClientSync(player, otherChest.getBlockPos());
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
