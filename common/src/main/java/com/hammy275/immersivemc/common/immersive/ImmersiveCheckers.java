package com.hammy275.immersivemc.common.immersive;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RepeaterBlock;

import java.util.LinkedList;
import java.util.List;

public class ImmersiveCheckers {

    public static final List<CheckerFunction> CHECKERS = new LinkedList<>();

    static {
        CHECKERS.add(ImmersiveCheckers::isRepeater);

        for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
            CHECKERS.add(handler::isValidBlock);
        }
    }

    // Vanilla

    public static boolean isRepeater(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof RepeaterBlock;
    }

}
