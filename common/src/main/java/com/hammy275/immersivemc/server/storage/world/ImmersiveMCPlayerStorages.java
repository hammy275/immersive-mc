package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.server.ServerUtil;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Uses SavedData to hold player storage
 */
public class ImmersiveMCPlayerStorages extends SavedData {

    private static final int PLAYER_STORAGES_VERSION = 2;

    protected Map<UUID, List<ItemStack>> backpackCraftingItemsMap = new HashMap<>();

    private static ImmersiveMCPlayerStorages create() {
        return new ImmersiveMCPlayerStorages();
    }

    public static List<ItemStack> getBackpackCraftingStorage(Player player) {
        return getPlayerStorage(player).backpackCraftingItemsMap.computeIfAbsent(player.getUUID(), uuid -> {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i <= 4; i++) {
                items.add(ItemStack.EMPTY);
            }
            return items;
        });
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
        // Use 3700 for 1.20.4 (most recent Minecraft version with ImmersiveMC before this was added) or the current Minecraft data version, whichever is lower.
        int lastVanillaDataVersion = nbt.contains("lastVanillaDataVersion") ? nbt.getInt("lastVanillaDataVersion") : Math.min(3700, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        nbt = maybeUpgradeNBT(nbt, lastVanillaDataVersion);
        Set<String> keys = nbt.getAllKeys();
        for (String uuidStr : keys) {
            UUID uuid = UUID.fromString(uuidStr);
            CompoundTag bagItems = nbt.getCompound(uuidStr).getCompound("bagItems");
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i <= 4; i++) {
                items.add(ServerUtil.parseItem(bagItems.getCompound(String.valueOf(i)), lastVanillaDataVersion));
            }
            playerStorage.backpackCraftingItemsMap.put(uuid, items);
        }
        return playerStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("lastVanillaDataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        for (Map.Entry<UUID, List<ItemStack>> entry : backpackCraftingItemsMap.entrySet()) {
            CompoundTag playerData = new CompoundTag();
            CompoundTag bagData = new CompoundTag();
            List<ItemStack> items = entry.getValue();
            for (int i = 0; i <= 4; i++) {
                CompoundTag itemData = new CompoundTag();
                if (i >= items.size()) {
                    itemData = ItemStack.EMPTY.save(itemData);
                } else {
                    itemData = items.get(i).save(itemData);
                }
                bagData.put(String.valueOf(i), itemData);
            }
            playerData.put("bagItems", bagData);
            nbt.put(String.valueOf(entry.getKey()), playerData);
        }
        return nbt;
    }

    /**
     * Upgrades NBT tag to something this version of ImmersiveMC can understand.
     * @param nbtIn NBT to upgrade. This may be modified in any way.
     * @param lastVanillaDataVersion The last vanilla data version this saved data was saved in.
     * @return A converted NBT, that isn't necessarily the same object as the nbt going into this function.
     */
    private static CompoundTag maybeUpgradeNBT(CompoundTag nbtIn, int lastVanillaDataVersion) {
        int version = 1;
        if (nbtIn.contains("version")) { // Version 1 didn't store a version int
            version = nbtIn.getInt("version");
        }
        while (version < PLAYER_STORAGES_VERSION) {
            if (version == 1) {
                CompoundTag newNBT = new CompoundTag();
                Set<String> keys = nbtIn.getAllKeys();
                for (String uuidStr : keys) {
                    CompoundTag oldPlayerData = nbtIn.getCompound(uuidStr);
                    CompoundTag bagItems = oldPlayerData.getCompound("storages").getCompound("0").getCompound("data");
                    bagItems.remove("identifier");
                    bagItems.remove("numOfItems");
                    for (int i = 0; i <= 4; i++) {
                        CompoundTag itemData = bagItems.getCompound("item" + i);
                        bagItems.remove("item" + i);
                        bagItems.put(String.valueOf(i), itemData);
                    }
                    // {UUID: {bagItems: {...}}}
                    CompoundTag newPlayerData = new CompoundTag();
                    newPlayerData.put("bagItems", bagItems);
                    newNBT.put(uuidStr, newPlayerData);
                }
                nbtIn = newNBT;
            }
            version++;
        }
        return nbtIn;
    }
}
