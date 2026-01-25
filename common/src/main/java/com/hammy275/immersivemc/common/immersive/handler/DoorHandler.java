package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoorHandler implements ImmersiveHandler<NullStorage>{
    @Override
    public NullStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public NullStorage getEmptyNetworkStorage() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        // Doors don't swap
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof DoorBlock db && db.type().canOpenByHand();
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useDoorImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("door");
    }
}
