package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class HopperHandler extends ChestLikeHandler<ListOfItemsStorage> {
    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof HopperBlockEntity;
    }

    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer tracker, BlockPos pos) {
        return makeBaseInventoryContents(tracker, pos);
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return getBaseEmptyNetworkStorage();
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useHopperImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("hopper");
    }
}
