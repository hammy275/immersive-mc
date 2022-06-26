package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.server.storage.info.ImmersiveStorage;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class WorldStorage extends WorldSavedData {

    protected Map<BlockPos, ImmersiveStorage> itemInfo = new HashMap<>();

    public WorldStorage() {
        super("immersivemc_data");
    }

    public static boolean usesWorldStorage(BlockState state) {
        return state.getBlock() == Blocks.CRAFTING_TABLE ||
                state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock
                || state.getBlock() instanceof EnchantingTableBlock;
    }

    public static WorldStorage getStorage(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(WorldStorage::new, "immersivemc_data");
    }

    public static WorldStorage getStorage(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity spe = (ServerPlayerEntity) player;
            return getStorage((ServerWorld) player.level);
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
                storage = new ImmersiveStorage(this);
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
