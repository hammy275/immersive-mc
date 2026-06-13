package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SharedNetworkStoragesImpl implements SharedNetworkStorages {

    public static final SharedNetworkStoragesImpl INSTANCE = new SharedNetworkStoragesImpl();

    private Map<ResourceLocation, Map<BlockPos, NetworkStorage>> storages = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <S extends NetworkStorage> S getOrCreate(Level level, BlockPos pos, BlockBasedImmersiveHandler<S> handler) {
        S emptyNetworkStorage = handler.getEmptyNetworkStorage();
        return getOrCreate(level, pos, (Class<S>) emptyNetworkStorage.getClass(), () -> emptyNetworkStorage);
    }

    @Override
    public <S extends NetworkStorage> S getOrCreate(Level level, BlockPos pos, Class<S> type, Supplier<S> storageCreator) {
        S storage = get(level, pos, type);
        if (storage == null) {
            storage = storageCreator.get();
            storages.get(level.dimension().location()).put(pos, storage);
        }
        return storage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends NetworkStorage> @Nullable S get(Level level, BlockPos pos, BlockBasedImmersiveHandler<S> handler) {
        return (S) get(level, pos, handler.getEmptyNetworkStorage().getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends NetworkStorage> @Nullable S get(Level level, BlockPos pos, Class<S> type) {
        Map<BlockPos, NetworkStorage> innerMap = storages.computeIfAbsent(level.dimension().location(), rl -> new HashMap<>());
        if (innerMap.containsKey(pos)) {
            NetworkStorage storage = innerMap.get(pos);
            if (storage.getClass() == type) {
                return (S) storage;
            }
        }
        return null;
    }

    @Override
    public <S extends NetworkStorage> void remove(Level level, BlockPos pos, BlockBasedImmersiveHandler<S> handler) {
        remove(level, pos, handler.getEmptyNetworkStorage().getClass());
    }

    @Override
    public <S extends NetworkStorage> void remove(Level level, BlockPos pos, Class<S> type) {
        Map<BlockPos, NetworkStorage> innerMap = storages.get(level.dimension().location());
        if (innerMap != null) {
            NetworkStorage storage = innerMap.get(pos);
            if (storage != null && storage.getClass() == type) {
                innerMap.remove(pos);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends NetworkStorage> List<S> getAll(Class<S> storageClass) {
        return storages.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(storage -> storage.getClass() == storageClass)
                .map(storage -> (S) storage)
                .toList();
    }

    /**
     * Clears all storages. Used by clients disconnecting to clear storages for singleplayer worlds.
     */
    public void clear() {
        storages.clear();
    }
}
