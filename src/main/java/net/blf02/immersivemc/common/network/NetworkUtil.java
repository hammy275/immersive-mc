package net.blf02.immersivemc.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class NetworkUtil {

    public static boolean safeToRun(BlockPos pos, ServerPlayerEntity runner) {
        return runner != null && runner.level.isLoaded(pos) &&
                runner.distanceToSqr(Vector3d.atCenterOf(pos)) <= 256;
        // Within 16 blocks (no square root)
    }
}
