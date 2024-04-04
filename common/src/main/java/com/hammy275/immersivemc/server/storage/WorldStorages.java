package com.hammy275.immersivemc.server.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class WorldStorages {

    /**
     * Gets the WorldStorage at the given location.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances.
     */
    public static WorldStorage get(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).getOrCreate(pos, level);
    }

    /**
     * Remove the WorldStorage at the given location.
     * @param pos Position of block potentially with a WorldStorage.
     * @param level Level the block is in.
     * @return The WorldStorage instance that was removed, or null if nothing was removed.
     */
    public static WorldStorage remove(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).remove(pos);
    }

    /**
     * Marks the WorldStorage for the given level as dirty, meaning on next world save, the WorldStorage instances
     * will be saved.
     * @param level Level that had a WorldStorage change.
     */
    public static void markDirty(ServerLevel level) {
        ImmersiveMCLevelStorage.getLevelStorage(level).setDirty();
    }

}
