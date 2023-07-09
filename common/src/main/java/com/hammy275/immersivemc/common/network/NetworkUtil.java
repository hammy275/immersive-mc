package com.hammy275.immersivemc.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetworkUtil {

    public static boolean safeToRun(BlockPos pos, ServerPlayer runner) {
        return runner != null && runner.level().isLoaded(pos) &&
                runner.distanceToSqr(Vec3.atCenterOf(pos)) <= 256;
        // Within 16 blocks (no square root)
    }
}
