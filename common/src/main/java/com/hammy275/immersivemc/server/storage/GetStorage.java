package com.hammy275.immersivemc.server.storage;

import com.hammy275.immersivemc.common.storage.AnvilWorldStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;

public class GetStorage {

    public static ImmersiveStorage assembleStorage(CompoundTag nbt, String storageType, SavedData wStorage) {
        // Check storage type, and load storage accordingly
        ImmersiveStorage storage = null;
        if (storageType.equals(ImmersiveStorage.TYPE)) {
            storage = new ImmersiveStorage(wStorage);
            storage.load(nbt);
        } else if (storageType.equals(AnvilWorldStorage.TYPE)) {
            storage = new AnvilWorldStorage(wStorage);
            storage.load(nbt);
        }

        if (storage == null) {
            throw new IllegalArgumentException("Storage type " + storageType + " does not exist!");
        }
        return storage;
    }

    public static ImmersiveStorage getPlayerStorage(Player player, String playerStorageKey) {
        List<ImmersiveStorage> storages = ImmersiveMCPlayerStorages.getStorages(player);
        for (ImmersiveStorage storage : storages) {
            if (storage.identifier.equals("backpack")) {
                return storage;
            }
        }
        if (playerStorageKey.equals("backpack")) {
            ImmersiveStorage storage = new ImmersiveStorage(ImmersiveMCPlayerStorages.getPlayerStorage(player)).initIfNotAlready(5);
            storage.identifier = "backpack";
            ImmersiveMCPlayerStorages.getStorages(player).add(storage);
            ImmersiveMCPlayerStorages.getPlayerStorage(player).setDirty();
            return storage;
        }
        throw new IllegalArgumentException("Invalid player storage type!");
    }

    public static ImmersiveStorage getCraftingStorage(Player player, BlockPos pos) {
        return ImmersiveMCLevelStorage.getLevelStorage(player).getOrCreate(pos).initIfNotAlready(10);
    }
}
