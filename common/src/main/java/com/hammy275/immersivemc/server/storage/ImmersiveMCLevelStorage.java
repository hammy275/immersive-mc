package com.hammy275.immersivemc.server.storage;

import com.hammy275.immersivemc.common.immersive.CheckerFunction;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds ALL OF the save data for ImmersiveMC for a given world/dimension.
 */
public class ImmersiveMCLevelStorage extends SavedData {

    protected Map<BlockPos, ImmersiveStorage> itemInfo = new HashMap<>();

    private static ImmersiveMCLevelStorage create() {
        return new ImmersiveMCLevelStorage();
    }

    public static boolean usesWorldStorage(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> checker : ImmersiveCheckers.WORLD_STORAGE_CHECKERS) {
            if (checker.apply(pos, state, tileEntity, level)) {
                return true;
            }
        }
        return false;
    }

    public static ImmersiveMCLevelStorage getLevelStorage(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(ImmersiveMCLevelStorage::load, ImmersiveMCLevelStorage::create, "immersivemc_data");
    }

    public static ImmersiveMCLevelStorage getLevelStorage(Player player) {
        if (player instanceof ServerPlayer) {
            return getLevelStorage((ServerLevel) player.level);
        }
        throw new IllegalArgumentException("Can only get storage server side!");
    }

    public ImmersiveStorage getOrCreate(BlockPos pos) {
        ImmersiveStorage storage = get(pos);
        if (storage == null) {
            storage = new ImmersiveStorage(this);
            add(pos, storage);
        }
        return storage;
    }


    public ImmersiveStorage remove(BlockPos pos) {
        ImmersiveStorage storage = itemInfo.remove(pos);
        this.setDirty();
        return storage;
    }

    public void add(BlockPos pos, ImmersiveStorage storage) {
        itemInfo.put(pos, storage);
        this.setDirty();
    }

    public ImmersiveStorage get(BlockPos pos) {
        return itemInfo.get(pos);
    }


    public static ImmersiveMCLevelStorage load(CompoundTag nbt) {
        ImmersiveMCLevelStorage levelStorage = new ImmersiveMCLevelStorage();
        Map<BlockPos, ImmersiveStorage> itemInfo = levelStorage.itemInfo;
        itemInfo.clear();
        int numOfStorages = nbt.getInt("numOfStorages");

        CompoundTag storages = nbt.getCompound("storages");
        for (int i = 0; i < numOfStorages; i++) {
            CompoundTag storageInfo = storages.getCompound(String.valueOf(i));

            BlockPos pos = new BlockPos(storageInfo.getInt("posX"),
                    storageInfo.getInt("posY"),
                    storageInfo.getInt("posZ"));

            String storageType = storageInfo.getString("dataType");
            ImmersiveStorage storage = GetStorage.assembleStorage(storageInfo.getCompound("data"),
                    storageType, levelStorage);

            itemInfo.put(pos, storage);

        }
        return levelStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("numOfStorages", itemInfo.size());
        CompoundTag storages = new CompoundTag();
        int i = 0;
        for (Map.Entry<BlockPos, ImmersiveStorage> entry : itemInfo.entrySet()) {
            CompoundTag storageInfo = new CompoundTag();

            storageInfo.putInt("posX", entry.getKey().getX());
            storageInfo.putInt("posY", entry.getKey().getY());
            storageInfo.putInt("posZ", entry.getKey().getZ());
            storageInfo.put("data", entry.getValue().save(new CompoundTag()));
            storageInfo.putString("dataType", entry.getValue().getType());

            storages.put(String.valueOf(i), storageInfo);
            i++;
        }

        nbt.put("storages", storages);
        return nbt;
    }
}
