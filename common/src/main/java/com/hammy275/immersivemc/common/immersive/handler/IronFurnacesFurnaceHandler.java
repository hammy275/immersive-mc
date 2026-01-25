package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class IronFurnacesFurnaceHandler extends FurnaceHandler {

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return IronFurnaces.ironFurnaceTileBase.isInstance(level.getBlockEntity(pos));
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useIronFurnacesFurnaceImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("iron_furnaces_furnace");
    }

    @Override
    protected void awardXP(WorldlyContainer furnace, ServerPlayer player) {
        IronFurnaces.doUnlockRecipes(furnace, player);
    }
}
