package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxHandler extends ChestLikeHandler<ListOfItemsStorage> {

    @Override
    public boolean canPlaceItem(ItemStack item) {
        return !(Block.byItem(item.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock && super.isValidBlock(pos, level);
    }

    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return makeBaseInventoryContents(player, pos);
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return getBaseEmptyNetworkStorage();
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useShulkerImmersive;
    }

    @Override
    public void onStopTracking(ServerPlayer player, BlockPos pos) {
        super.onStopTracking(player, pos);
        BlockEntity shulkerBox = player.level().getBlockEntity(pos);
        if (shulkerBox instanceof ShulkerBoxBlockEntity sbbe) {
            sbbe.stopOpen(player);
        }
        Lootr.lootrImpl.openLootrShulkerBox(pos, player, false);
    }

    @Override
    public Identifier getID() {
        return Util.id("shulker_box");
    }
}
