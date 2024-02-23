package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class ImmersiveCheckers {

    public static final List<CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean>>
            CHECKERS = new LinkedList<>();
    public static final List<CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean>>
            WORLD_STORAGE_CHECKERS = new LinkedList<>();

    static {
        CHECKERS.add(ImmersiveCheckers::isIronFurnacesFurnace);
        CHECKERS.add(ImmersiveCheckers::isRepeater);
        CHECKERS.add(ImmersiveCheckers::isTinkersConstructCraftingStation);

        for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
            CHECKERS.add(handler::isValidBlock);
            if (handler.usesWorldStorage()) {
                WORLD_STORAGE_CHECKERS.add(handler::isValidBlock);
            }
        }
    }

    // Vanilla

    public static boolean isRepeater(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() instanceof RepeaterBlock;
    }

    // IronFurnaces

    public static boolean isIronFurnacesFurnace(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return IronFurnaces.ironFurnaceTileBase.isInstance(tileEntity);
    }

    // Tinkers' Construct
    public static boolean isTinkersConstructCraftingStation(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return TinkersConstruct.craftingStation.isInstance(tileEntity);
    }

}
