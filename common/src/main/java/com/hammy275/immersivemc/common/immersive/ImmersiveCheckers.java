package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.common.compat.IronFurnaces;
import com.hammy275.immersivemc.common.compat.TinkersConstruct;
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
        CHECKERS.add(ImmersiveCheckers::isBarrel);
        CHECKERS.add(ImmersiveCheckers::isChest);
        CHECKERS.add(ImmersiveCheckers::isChiseledBookshelf);
        CHECKERS.add(ImmersiveCheckers::isHopper);
        CHECKERS.add(ImmersiveCheckers::isIronFurnacesFurnace);
        CHECKERS.add(ImmersiveCheckers::isJukebox);
        CHECKERS.add(ImmersiveCheckers::isRepeater);
        CHECKERS.add(ImmersiveCheckers::isShulkerBox);
        CHECKERS.add(ImmersiveCheckers::isSmithingTable);
        CHECKERS.add(ImmersiveCheckers::isTinkersConstructCraftingStation);

        WORLD_STORAGE_CHECKERS.add(ImmersiveCheckers::isSmithingTable);

        for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
            CHECKERS.add(handler::isValidBlock);
            if (handler.usesWorldStorage()) {
                WORLD_STORAGE_CHECKERS.add(handler::isValidBlock);
            }
        }
    }

    // Vanilla

    public static boolean isBarrel(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof BarrelBlockEntity;
    }
    
    public static boolean isChest(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof ChestBlockEntity || tileEntity instanceof EnderChestBlockEntity;
    }

    public static boolean isChiseledBookshelf(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() == Blocks.CHISELED_BOOKSHELF;
    }

    public static boolean isHopper(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof HopperBlockEntity;
    }

    public static boolean isJukebox(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof JukeboxBlockEntity;
    }

    public static boolean isRepeater(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() instanceof RepeaterBlock;
    }

    public static boolean isShulkerBox(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof ShulkerBoxBlockEntity;
    }

    public static boolean isSmithingTable(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() instanceof SmithingTableBlock;
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
