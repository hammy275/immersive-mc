package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public class VisualWorkbenchHandler extends TCCraftingStationHandler {

    private static final String visualWorkbenchClass = "fuzs.visualworkbench.world.level.block.CraftingTableWithInventoryBlock";

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos) {
        return DirtyTracker.isDirty(tracker.level(), pos);
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount) {
        super.swap(slot, hand, pos, tracker, amount);
        BaseContainerBlockEntity table = (BaseContainerBlockEntity) tracker.level().getBlockEntity(pos);
        // Menu creation is the way to update the client of the changed block entity state.
        table.createMenu(-1, tracker.getInventory(), tracker);
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        Block block = level.getBlockState(pos).getBlock();
        return (CraftingHandler.isCraftingTableBlock(block) || block.getClass().getName().equals(visualWorkbenchClass))
                && level.getBlockEntity(pos) instanceof Container;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useCraftingTableImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("visual_workbench");
    }
}
