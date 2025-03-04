package com.hammy275.immersivemc.server.storage.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class WorldStoragesImpl implements WorldStorages {

    public static final WorldStorages INSTANCE = new WorldStoragesImpl();

    /**
     * Gets the WorldStorage at the given location, or creates a new one if needed.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances.
     */
    public static WorldStorage getOrCreateS(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).getOrCreate(pos, level);
    }

    /**
     * Gets the WorldStorage already present at the given location.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances or if there isn't a WorldStorage already there.
     */
    public static WorldStorage getS(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).get(pos, level);
    }

    /**
     * Gets the WorldStorage already present at the given location even if it doesn't match the present block.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances or if there isn't a WorldStorage already there.
     */
    public static WorldStorage getWithoutVerificationS(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).getWithoutVerification(pos, level);
    }

    /**
     * Remove the WorldStorage at the given location.
     * @param pos Position of block potentially with a WorldStorage.
     * @param level Level the block is in.
     * @return The WorldStorage instance that was removed, or null if nothing was removed.
     */
    public static WorldStorage removeS(BlockPos pos, ServerLevel level) {
        return ImmersiveMCLevelStorage.getLevelStorage(level).remove(pos);
    }

    /**
     * Marks the WorldStorage for the given level as dirty, meaning on next world save, the WorldStorage instances
     * will be saved.
     * @param level Level that had a WorldStorage change.
     */
    public static void markDirtyS(ServerLevel level) {
        ImmersiveMCLevelStorage.getLevelStorage(level).setDirty();
    }

    @Override
    public WorldStorage getOrCreate(BlockPos pos, ServerLevel level) {
        return getOrCreateS(pos, level);
    }

    @Override
    public WorldStorage get(BlockPos pos, ServerLevel level) {
        return getS(pos, level);
    }

    @Override
    public WorldStorage getWithoutVerification(BlockPos pos, ServerLevel level) {
        return getWithoutVerificationS(pos, level);
    }

    @Override
    public WorldStorage remove(BlockPos pos, ServerLevel level) {
        return removeS(pos, level);
    }

    @Override
    public void markDirty(ServerLevel level) {
        markDirtyS(level);
    }
}
