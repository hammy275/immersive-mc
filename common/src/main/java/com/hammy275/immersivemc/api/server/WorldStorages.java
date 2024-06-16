package com.hammy275.immersivemc.api.server;

import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Contains methods for interacting with ImmersiveMC's WorldStorage system.
 */
public interface WorldStorages {

    /**
     * @return A WorldStorages instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static WorldStorages instance() {
        return WorldStoragesImpl.INSTANCE;
    }

    /**
     * Gets the WorldStorage at the given location, or creates a new one if needed.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances.
     */
    public WorldStorage getOrCreate(BlockPos pos, ServerLevel level);

    /**
     * Gets the WorldStorage already present at the given location.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances or if there isn't a WorldStorage already there.
     */
    public WorldStorage get(BlockPos pos, ServerLevel level);

    /**
     * Gets the WorldStorage already present at the given location even if it doesn't match the present block.
     * @param pos Position of block with a WorldStorage and/or associated with a handler.
     * @param level Level the block is in.
     * @return A WorldStorage instance for the given block position, or null if the block isn't associated with any
     *         WorldStorageHandler instances or if there isn't a WorldStorage already there.
     */
    public WorldStorage getWithoutVerification(BlockPos pos, ServerLevel level);

    /**
     * Remove the WorldStorage at the given location.
     * @param pos Position of block potentially with a WorldStorage.
     * @param level Level the block is in.
     * @return The WorldStorage instance that was removed, or null if nothing was removed.
     */
    public WorldStorage remove(BlockPos pos, ServerLevel level);

    /**
     * Marks the WorldStorage for the given level as dirty, meaning on next world save, the WorldStorage instances
     * will be saved.
     * @param level Level that had a WorldStorage change.
     */
    public void markDirty(ServerLevel level);
}
