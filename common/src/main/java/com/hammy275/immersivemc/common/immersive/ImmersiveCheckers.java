package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.common.compat.IronFurnaces;
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

    static {
        CHECKERS.add(ImmersiveCheckers::isAnvil);
        CHECKERS.add(ImmersiveCheckers::isBeacon);
        CHECKERS.add(ImmersiveCheckers::isBrewingStand);
        CHECKERS.add(ImmersiveCheckers::isChest);
        CHECKERS.add(ImmersiveCheckers::isCraftingTable);
        CHECKERS.add(ImmersiveCheckers::isEnchantingTable);
        CHECKERS.add(ImmersiveCheckers::isFurnace);
        CHECKERS.add(ImmersiveCheckers::isJukebox);
        CHECKERS.add(ImmersiveCheckers::isRepeater);
        CHECKERS.add(ImmersiveCheckers::isShulkerBox);
    }

    public static boolean isAnvil(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock;
    }

    public static boolean isBeacon(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof BeaconBlockEntity;
    }

    public static boolean isBrewingStand(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof BrewingStandBlockEntity;
    }
    
    public static boolean isChest(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof ChestBlockEntity || tileEntity instanceof EnderChestBlockEntity;
    }

    public static boolean isCraftingTable(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() == Blocks.CRAFTING_TABLE ||
                (tileEntity != null &&
                        tileEntity.getClass().getName().equals("slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity"));
    }

    public static boolean isEnchantingTable(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return state.getBlock() instanceof EnchantmentTableBlock;
    }

    public static boolean isFurnace(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return tileEntity instanceof AbstractFurnaceBlockEntity ||
                IronFurnaces.ironFurnaceTileBase.isInstance(tileEntity);
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


}
