package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;

import java.util.LinkedList;
import java.util.List;

public class ImmersiveCheckers {

    public static final List<ImmersiveChecker> CHECKERS = new LinkedList<>();

    static {
        for (ImmersiveHandler<?> handler : ImmersiveHandlers.HANDLERS) {
            CHECKERS.add((pos, level) -> Util.isValidBlocks(handler, pos, level));
        }
    }

    // Vanilla

    public static boolean isLever(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof LeverBlock;
    }

    public static boolean isRepeater(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof RepeaterBlock;
    }

}
