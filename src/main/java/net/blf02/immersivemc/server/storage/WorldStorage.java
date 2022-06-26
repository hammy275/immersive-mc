package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.server.storage.info.ImmersiveStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class WorldStorage extends WorldSavedData {

    public Map<BlockPos, ImmersiveStorage> itemInfo = new HashMap<>();

    public WorldStorage() {
        super("immersivemc_data");
    }

    @Override
    public void load(CompoundNBT nbt) {
        itemInfo.clear();
        int numOfStorages = nbt.getInt("numOfStorages");

        CompoundNBT storages = nbt.getCompound("storages");
        for (int i = 0; i < numOfStorages; i++) {
            CompoundNBT storageInfo = storages.getCompound(String.valueOf(i));

            BlockPos pos = new BlockPos(storageInfo.getInt("posX"),
                    storageInfo.getInt("posY"),
                    storageInfo.getInt("posZ"));

            ImmersiveStorage storage = null;
            String storageType = storageInfo.getString("dataType");

            // Check storage type, and load storage accordingly
            if (storageType.equals(ImmersiveStorage.TYPE)) {
                storage = new ImmersiveStorage();
                storage.load(storageInfo.getCompound("data"));
            }

            if (storage == null) {
                throw new IllegalArgumentException("Storage type " + storageType + " does not exist!");
            }

            itemInfo.put(pos, storage);

        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putInt("numOfStorages", itemInfo.size());
        CompoundNBT storages = new CompoundNBT();
        int i = 0;
        for (Map.Entry<BlockPos, ImmersiveStorage> entry : itemInfo.entrySet()) {
            CompoundNBT storageInfo = new CompoundNBT();

            storageInfo.putInt("posX", entry.getKey().getX());
            storageInfo.putInt("posY", entry.getKey().getY());
            storageInfo.putInt("posZ", entry.getKey().getZ());
            storageInfo.put("data", entry.getValue().save(new CompoundNBT()));
            storageInfo.putString("dataType", entry.getValue().getType());

            storages.put(String.valueOf(i), storageInfo);
            i++;
        }

        nbt.put("storages", storages);
        return nbt;
    }
}
