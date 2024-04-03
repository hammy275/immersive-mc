package com.hammy275.immersivemc.server.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class WorldStorages {

    public static WorldStorage getStorageForBlock(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorageV2.getLevelStorage(level).getOrCreate(pos, level);
    }

    public static void markDirty(ServerLevel level) {
        ImmersiveMCLevelStorageV2.getLevelStorage(level).setDirty();
    }

}
