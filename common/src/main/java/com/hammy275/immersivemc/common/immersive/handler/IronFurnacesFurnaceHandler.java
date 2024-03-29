package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.IronFurnaces;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class IronFurnacesFurnaceHandler extends FurnaceHandler {

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return IronFurnaces.ironFurnaceTileBase.isInstance(level.getBlockEntity(pos));
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "iron_furnaces_furnace");
    }
}
