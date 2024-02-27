package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChestHandler extends ChestLikeHandler {
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
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
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
