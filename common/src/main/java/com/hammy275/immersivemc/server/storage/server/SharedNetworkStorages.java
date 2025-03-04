package com.hammy275.immersivemc.server.storage.server;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.server.api_impl.SharedNetworkStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A place to keep {@link NetworkStorage} instances. These are not saved to disk, but are kept as long as the
 * immersive block exists and is tracked by at least one player. Useful for keeping shared state that can
 * be synced to players if they start tracking an Immersive.
 */
public interface SharedNetworkStorages {

    /**
     * @return A SharedNetworkStorages instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static SharedNetworkStorages instance() {
        return SharedNetworkStoragesImpl.INSTANCE;
    }

    /**
     * Gets the NetworkStorage saved at the specified position, or creates a new one if one isn't already there.
     * @param level The level to get the storage from. This should be the level the Immersive is in.
     * @param pos The position to get the storage from. This should be the block position the Immersive is at.
     * @param handler The {@link ImmersiveHandler} for the Immersive.
     * @return An instance of S, which is the NetworkStorage at that position. It may have been freshly created via
     * {@link ImmersiveHandler#getEmptyNetworkStorage()}, so it may need initializing.
     * @param <S> The type of storage used by the ImmersiveHandler.
     */
    public <S extends NetworkStorage> S getOrCreate(Level level, BlockPos pos, ImmersiveHandler<S> handler);

    /**
     * Gets the NetworkStorage saved at the specified position, or returns null if one isn't already there.
     * @param level The level to get the storage from. This should be the level the Immersive is in.
     * @param pos The position to get the storage from. This should be the block position the Immersive is at.
     * @param handler The {@link ImmersiveHandler} for the Immersive.
     * @return An instance of S, which is the NetworkStorage at that position, or null if a NetworkStorage either
     * wasn't there, or did not match the type of your handler.
     * @param <S> The type of storage used by the ImmersiveHandler.
     */
    @Nullable
    public <S extends NetworkStorage> S get(Level level, BlockPos pos, ImmersiveHandler<S> handler);

    /**
     * Removes the NetworkStorage saved at the specified position, or does nothing if one isn't there or doesn't match
     * the storage type of the handler provided.
     * @param level The level to remove the storage from. This should be the level the Immersive is in.
     * @param pos The position to remove the storage from. This should be the block position the Immersive is at.
     * @param handler The {@link ImmersiveHandler} for the Immersive.
     * @param <S> The type of storage used by the ImmersiveHandler.
     */
    public <S extends NetworkStorage> void remove(Level level, BlockPos pos, ImmersiveHandler<S> handler);

    /**
     * Get all storages stored that match some class; useful for marking storages as no longer dirty for
     * client syncing. Note that the returned list is not guaranteed to be mutable.
     * @param storageClass The class of storages to get.
     * @return A list of all storages matching the provided class. The list is not guaranteed to be mutable.
     * @param <S> The type of storage to get a list of.
     */
    public <S extends NetworkStorage> List<S> getAll(Class<S> storageClass);
}
