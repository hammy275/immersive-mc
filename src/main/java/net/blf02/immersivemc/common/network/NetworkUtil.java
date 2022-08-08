package net.blf02.immersivemc.common.network;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.phys.Vec3;

public class NetworkUtil {

    public static boolean safeToRun(BlockPos pos, ServerPlayer runner) {
        return runner != null && runner.level.isLoaded(pos) &&
                runner.distanceToSqr(Vec3.atCenterOf(pos)) <= 256;
        // Within 16 blocks (no square root)
    }
}
