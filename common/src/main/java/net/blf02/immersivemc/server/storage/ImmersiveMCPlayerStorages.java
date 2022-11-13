package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Uses SavedData to hold player storage
 *
 * Not using capabilities because [REDACTED]
 */
public class ImmersiveMCPlayerStorages extends SavedData {

    protected Map<UUID, List<ImmersiveStorage>> playerStorages = new HashMap<>();

    private static ImmersiveMCPlayerStorages create() {
        return new ImmersiveMCPlayerStorages();
    }

    public static List<ImmersiveStorage> getStorages(Player player) {
        List<ImmersiveStorage> immersiveStorages = getPlayerStorage(player).playerStorages.get(player.getUUID());
        if (immersiveStorages == null) {
            immersiveStorages = new ArrayList<>();
            getPlayerStorage(player).playerStorages.put(player.getUUID(), immersiveStorages);
        }
        return immersiveStorages;
    }

    public static ImmersiveMCPlayerStorages getPlayerStorage(Player player) {
        if (!player.level.isClientSide) {
            ServerPlayer sPlayer = (ServerPlayer) player;
            return sPlayer.getServer().overworld().getDataStorage()
                    .computeIfAbsent(ImmersiveMCPlayerStorages::load, ImmersiveMCPlayerStorages::create, "immersivemc_player_data");
        }
        throw new IllegalArgumentException("Can only access storage on server-side!");
    }

    public static ImmersiveMCPlayerStorages load(CompoundTag nbt) {
        ImmersiveMCPlayerStorages playerStorage = new ImmersiveMCPlayerStorages();
        Set<String> keys = nbt.getAllKeys();
        for (String uuidStr : keys) {
            UUID uuid = UUID.fromString(uuidStr);
            CompoundTag playerTag = nbt.getCompound(uuidStr);
            CompoundTag storagesTag = playerTag.getCompound("storages");
            int numStorages = storagesTag.getInt("numStorages");
            List<ImmersiveStorage> storages = new ArrayList<>();
            for (int i = 0; i < numStorages; i++) {
                CompoundTag storageInfo = storagesTag.getCompound(String.valueOf(i));
                String storageType = storageInfo.getString("dataType");
                ImmersiveStorage storage = GetStorage.assembleStorage(storageInfo.getCompound("data"),
                        storageType, playerStorage);
                storages.add(storage);
            }
            playerStorage.playerStorages.put(uuid, storages);
        }
        return playerStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (Map.Entry<UUID, List<ImmersiveStorage>> entry : playerStorages.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            CompoundTag storagesTag = new CompoundTag();

            int numStorages = entry.getValue().size();
            storagesTag.putInt("numStorages", numStorages);
            for (int i = 0; i < numStorages; i++) {
                CompoundTag storageInfo = new CompoundTag();
                storageInfo.put("data", entry.getValue().get(i).save(new CompoundTag()));
                storageInfo.putString("dataType", entry.getValue().get(i).getType());
                storagesTag.put(String.valueOf(i), storageInfo);
            }

            playerTag.put("storages", storagesTag);
            nbt.put(entry.getKey().toString(), playerTag);
        }
        return nbt;
    }
}
