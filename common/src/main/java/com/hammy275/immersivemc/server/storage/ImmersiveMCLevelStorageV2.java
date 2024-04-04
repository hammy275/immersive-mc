package com.hammy275.immersivemc.server.storage;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds ALL the save data for ImmersiveMC for a given world/dimension.
 */
public class ImmersiveMCLevelStorageV2 extends SavedData {

    private static final int LEVEL_STORAGE_VERSION = 2;

    private static Factory<ImmersiveMCLevelStorageV2> factory = new Factory<>(
            ImmersiveMCLevelStorageV2::create,
            ImmersiveMCLevelStorageV2::load,
            null
    );
    protected Map<BlockPos, WorldStorage> storageMap = new HashMap<>();

    private static ImmersiveMCLevelStorageV2 create() {
        return new ImmersiveMCLevelStorageV2();
    }

    public static ImmersiveMCLevelStorageV2 getLevelStorage(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(factory, "immersivemc_data");
    }

    @Nullable
    public WorldStorage remove(BlockPos pos) {
        return storageMap.remove(pos);
    }

    @Nullable
    public WorldStorage getOrCreate(BlockPos pos, Level level) {
        WorldStorage storage = storageMap.get(pos);
        for (ImmersiveHandler handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
            if (handlerMaybeWS instanceof WorldStorageHandler handler) {
                if (handler.getWorldStorageClass().isInstance(storage) && handler.isValidBlock(pos, level)) {
                    return storage;
                }
            }
        }
        // At this point, we either didn't find a storage or the storage doesn't match with any handler

        if (storage != null) { // Storage in-memory doesn't match a handler. Make a new storage.
            for (ImmersiveHandler handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
                if (handlerMaybeWS instanceof WorldStorageHandler handler) {
                    if (handler.isValidBlock(pos, level)) {
                        storage = handler.getEmptyWorldStorage();
                        storageMap.put(pos, storage);
                        return storage;
                    }
                }
            }
        }
        // Storage wasn't in-memory and didn't match a handler. Return null.
        return null;
    }

    public static ImmersiveMCLevelStorageV2 load(CompoundTag nbt) {
        ImmersiveMCLevelStorageV2 levelStorage = new ImmersiveMCLevelStorageV2();
        maybeUpgradeNBT(nbt, levelStorage);
        Map<BlockPos, WorldStorage> storageMap = levelStorage.storageMap;
        storageMap.clear();
        int numOfStorages = nbt.getInt("numOfStorages");

        CompoundTag storages = nbt.getCompound("storages");
        for (int i = 0; i < numOfStorages; i++) {
            CompoundTag storageInfo = storages.getCompound(String.valueOf(i));

            BlockPos pos = new BlockPos(storageInfo.getInt("posX"),
                    storageInfo.getInt("posY"),
                    storageInfo.getInt("posZ"));

            ResourceLocation id = Util.getResourceLocation(storageInfo, "id");
            WorldStorage storage = null;
            for (ImmersiveHandler handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
                if (handlerMaybeWS.getID().equals(id) && handlerMaybeWS instanceof WorldStorageHandler handler) {
                    storage = handler.getEmptyWorldStorage();
                    storage.load(storageInfo.getCompound("data"));
                    break;
                }
            }

            if (storage != null) {
                storageMap.put(pos, storage);
            }
        }
        return levelStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("version", LEVEL_STORAGE_VERSION);
        nbt.putInt("numOfStorages", storageMap.size());
        CompoundTag storages = new CompoundTag();
        int i = 0;
        for (Map.Entry<BlockPos, WorldStorage> entry : this.storageMap.entrySet()) {
            CompoundTag storageInfo = new CompoundTag();

            storageInfo.putInt("posX", entry.getKey().getX());
            storageInfo.putInt("posY", entry.getKey().getY());
            storageInfo.putInt("posZ", entry.getKey().getZ());
            storageInfo.put("data", entry.getValue().save(new CompoundTag()));
            Util.putResourceLocation(storageInfo, "id", entry.getValue().getHandler().getID());

            storages.put(String.valueOf(i), storageInfo);
            i++;
        }

        nbt.put("storages", storages);
        return nbt;
    }

    private static void maybeUpgradeNBT(CompoundTag nbt, ImmersiveMCLevelStorageV2 storage) {
        // Updates the compound tag to something this version of ImmersiveMC can understand.
        int version = 1;
        if (nbt.contains("version")) {
            version = nbt.getInt("version");
        }
        while (version < LEVEL_STORAGE_VERSION) {
            if (version == 1) {
                // TODO: Write code to convert to version 2.
            }
            version++;
            storage.setDirty();
        }

    }
}
