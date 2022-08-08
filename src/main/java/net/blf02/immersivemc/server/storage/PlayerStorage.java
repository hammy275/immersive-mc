package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Uses WorldSavedData to hold player storage
 *
 * Not using capabilities because [REDACTED]
 */
public class PlayerStorage extends WorldSavedData {

    protected Map<UUID, List<ImmersiveStorage>> playerStorages = new HashMap<>();

    public PlayerStorage() {
        super("immersivemc_player_data");
    }

    public static List<ImmersiveStorage> getStorages(Player player) {
        List<ImmersiveStorage> immersiveStorages = getPlayerStorage(player).playerStorages.get(player.getUUID());
        if (immersiveStorages == null) {
            immersiveStorages = new ArrayList<>();
            getPlayerStorage(player).playerStorages.put(player.getUUID(), immersiveStorages);
        }
        return immersiveStorages;
    }

    public static PlayerStorage getPlayerStorage(Player player) {
        if (!player.level.isClientSide) {
            ServerPlayer sPlayer = (ServerPlayer) player;
            return sPlayer.getServer().overworld().getDataStorage()
                    .computeIfAbsent(PlayerStorage::new, "immersivemc_player_data");
        }
        throw new IllegalArgumentException("Can only access storage on server-side!");
    }

    @Override
    public void load(CompoundNBT nbt) {
        Set<String> keys = nbt.getAllKeys();
        for (String uuidStr : keys) {
            UUID uuid = UUID.fromString(uuidStr);
            CompoundNBT playerTag = nbt.getCompound(uuidStr);
            CompoundNBT storagesTag = playerTag.getCompound("storages");
            int numStorages = storagesTag.getInt("numStorages");
            List<ImmersiveStorage> storages = new ArrayList<>();
            for (int i = 0; i < numStorages; i++) {
                CompoundNBT storageInfo = storagesTag.getCompound(String.valueOf(i));
                String storageType = storageInfo.getString("dataType");
                ImmersiveStorage storage = GetStorage.assembleStorage(storageInfo.getCompound("data"),
                        storageType, this);
                storages.add(storage);
            }
            playerStorages.put(uuid, storages);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        for (Map.Entry<UUID, List<ImmersiveStorage>> entry : playerStorages.entrySet()) {
            CompoundNBT playerTag = new CompoundNBT();
            CompoundNBT storagesTag = new CompoundNBT();

            int numStorages = entry.getValue().size();
            storagesTag.putInt("numStorages", numStorages);
            for (int i = 0; i < numStorages; i++) {
                CompoundNBT storageInfo = new CompoundNBT();
                storageInfo.put("data", entry.getValue().get(i).save(new CompoundNBT()));
                storageInfo.putString("dataType", entry.getValue().get(i).getType());
                storagesTag.put(String.valueOf(i), storageInfo);
            }

            playerTag.put("storages", storagesTag);
            nbt.put(entry.getKey().toString(), playerTag);
        }
        return nbt;
    }
}
