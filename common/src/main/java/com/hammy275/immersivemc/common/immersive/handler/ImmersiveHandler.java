package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ImmersiveHandler {

    HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos);

    HandlerStorage getEmptyHandler();

    void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode);

    boolean usesWorldStorage();

    boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level);

    boolean enabledInServerConfig();

    ResourceLocation getID();
}
