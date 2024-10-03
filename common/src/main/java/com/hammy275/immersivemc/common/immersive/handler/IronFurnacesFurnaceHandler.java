package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "iron_furnaces_furnace");
    }
}
