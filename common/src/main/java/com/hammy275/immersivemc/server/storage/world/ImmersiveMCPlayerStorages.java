package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.mixin.SavedDataStorageAccessor;
import com.hammy275.immersivemc.server.ServerUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.*;

/**
 * Uses SavedData to hold player storage
 */
public class ImmersiveMCPlayerStorages extends SavedData {

    private static final int PLAYER_STORAGES_VERSION = 2;

    // These are special identifiers caught by SavedDataStorageMixin
    public static final Identifier V1_6_0_ALPHA3 = Util.mcId("immersivemc_player_data_v1_6_0_alpha3");
    public static final Identifier MC1_21_11_BELOW = Util.id("immersivemc_player_data_1_21_11_and_below");


    private static final Codec<ImmersiveMCPlayerStorages> savedDataCodec = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ImmersiveMCPlayerStorages, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(new Pair<>(ImmersiveMCPlayerStorages.load(CompoundTag.CODEC.parse(ops, input).getOrThrow(), (RegistryOps<Tag>) ops), input));
        }

        @Override
        public <T> DataResult<T> encode(ImmersiveMCPlayerStorages input, DynamicOps<T> ops, T prefix) {
            return CompoundTag.CODEC.encode(input.save(new CompoundTag(), (RegistryOps<Tag>) ops, (EndTag) prefix), ops, prefix);
        }
    };
    private static final SavedDataType<ImmersiveMCPlayerStorages> savedDataType = new SavedDataType<>(
            // Uses Minecraft namespace, since that's how world-upgrades should handle it.
            Util.id("immersivemc_player_data"),
            ImmersiveMCPlayerStorages::create,
            savedDataCodec,
            null
    );

    private static final SavedDataType<ImmersiveMCPlayerStorages> savedDataType160Alpha3 = new SavedDataType<>(
            // Uses Minecraft namespace, since that's how world-upgrades should handle it.
            V1_6_0_ALPHA3,
            ImmersiveMCPlayerStorages::create,
            savedDataCodec,
            null
    );
    private static final SavedDataType<ImmersiveMCPlayerStorages> savedDataTypeMC12111AndBelow = new SavedDataType<>(
            MC1_21_11_BELOW,
            ImmersiveMCPlayerStorages::create,
            savedDataCodec,
            null
    );

    protected Map<UUID, List<ItemStack>> backpackCraftingItemsMap = new HashMap<>();
    protected Set<UUID> disabledPlayers = new HashSet<>();

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
        if (!player.level().isClientSide()) {
            SavedDataStorage dataStorage = ((ServerPlayer) player).level().getServer().overworld().getDataStorage();
            ImmersiveMCPlayerStorages storages = dataStorage.computeIfAbsent(savedDataType);
            // Upgrading from past versions in order of priority (data from MC 1.21.11 and below is preferred over
            // 1.6.0 Alpha 3, since the latter was not out for long).
            ImmersiveMCPlayerStorages mc12111AndBelow = dataStorage.get(savedDataTypeMC12111AndBelow);
            ImmersiveMCPlayerStorages v160Alpha3 = dataStorage.get(savedDataType160Alpha3);
            LinkedList<ImmersiveMCPlayerStorages> oldStorages = new LinkedList<>();
            oldStorages.add(mc12111AndBelow);
            oldStorages.add(v160Alpha3);
            oldStorages.removeIf(Objects::isNull);

            if (oldStorages.isEmpty()) {
                // Empty. We're either creating a storage on a fresh install, or loading the pre-existing storage that's
                // where we want it to be.
                return storages;
            }
            // We have at least one storage we're upgrading from.
            while (!oldStorages.isEmpty()) {
                mergeInOldStorages(storages, oldStorages.removeFirst());
            }
            // We very much want to clean up the data we're upgrading from (to prevent constantly restoring from it),
            // though only after we force a save.
            storages.setDirty();
            dataStorage.saveAndJoin();
            SavedDataStorageAccessor dataStorageAccessor = (SavedDataStorageAccessor) dataStorage;
            if (mc12111AndBelow != null) {
                dataStorageAccessor.immersiveMC$getDataFile(MC1_21_11_BELOW).toFile().delete();
                dataStorageAccessor.immersiveMC$getCache().remove(savedDataTypeMC12111AndBelow);
            }
            if (v160Alpha3 != null) {
                dataStorageAccessor.immersiveMC$getDataFile(V1_6_0_ALPHA3).toFile().delete();
                dataStorageAccessor.immersiveMC$getCache().remove(savedDataType160Alpha3);
            }
            return storages;
        }
        throw new IllegalArgumentException("Can only access storage on server-side!");
    }

    private static void mergeInOldStorages(ImmersiveMCPlayerStorages storages, ImmersiveMCPlayerStorages oldStorages) {
        if (storages.disabledPlayers.isEmpty() && !oldStorages.disabledPlayers.isEmpty()) {
            storages.disabledPlayers.addAll(oldStorages.disabledPlayers);
        }
        // Doing a loop like this, since for ImmersiveMC 1.6.0 Alpha 3, this upgrade process did not occur.
        // As such, we don't want to remove data that may already be in the map.
        // During a regular upgrade, every entry will get added on the first merge, since what's being merged into
        // is in its default (empty) state.
        for (Map.Entry<UUID, List<ItemStack>> entry : oldStorages.backpackCraftingItemsMap.entrySet()) {
            if (!storages.backpackCraftingItemsMap.containsKey(entry.getKey())) {
                storages.backpackCraftingItemsMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static boolean isPlayerDisabled(Player player) {
        return getPlayerStorage(player).disabledPlayers.contains(player.getUUID());
    }

    public static void setPlayerDisabled(Player player) {
       ImmersiveMCPlayerStorages storage = getPlayerStorage(player);
       storage.disabledPlayers.add(player.getUUID());
       storage.setDirty();
    }

    public static void setPlayerEnabled(Player player) {
        ImmersiveMCPlayerStorages storage = getPlayerStorage(player);
        storage.disabledPlayers.remove(player.getUUID());
        storage.setDirty();
    }

    public static ImmersiveMCPlayerStorages load(CompoundTag nbt, RegistryOps<Tag> ops) {
        ImmersiveMCPlayerStorages playerStorage = new ImmersiveMCPlayerStorages();
        // Use 3700 for 1.20.4 (most recent Minecraft version with ImmersiveMC before this was added) or the current Minecraft data version, whichever is lower.
        int lastVanillaDataVersion = nbt.contains("lastVanillaDataVersion") ? nbt.getInt("lastVanillaDataVersion").get() : Math.min(3700, SharedConstants.getCurrentVersion().dataVersion().version());
        nbt = maybeUpgradeNBT(nbt, lastVanillaDataVersion);
        Set<String> keys = nbt.keySet();
        for (String uuidStr : keys) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag bagItems = nbt.getCompound(uuidStr).get().getCompound("bagItems").get();
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i <= 4; i++) {
                    items.add(ServerUtil.parseItem(ops, bagItems.getCompound(String.valueOf(i)).get(), lastVanillaDataVersion));
                }
                playerStorage.backpackCraftingItemsMap.put(uuid, items);
            } catch (IllegalArgumentException ignored) {} // We also store non-UUID keys here.
        }
        CompoundTag disabledPlayers = nbt.contains("disabledPlayers") ? nbt.getCompound("disabledPlayers").get() : null;
        if (disabledPlayers != null) {
            for (String key : disabledPlayers.keySet()) {
                if (disabledPlayers.getString(key).get().equalsIgnoreCase("true")) {
                    playerStorage.disabledPlayers.add(UUID.fromString(key));
                }
            }
        }
        return playerStorage;
    }

    public CompoundTag save(CompoundTag nbt, RegistryOps<Tag> ops, EndTag prefix) {
        nbt.putInt("lastVanillaDataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        nbt.putInt("version", PLAYER_STORAGES_VERSION);
        for (Map.Entry<UUID, List<ItemStack>> entry : backpackCraftingItemsMap.entrySet()) {
            CompoundTag playerData = new CompoundTag();
            CompoundTag bagData = new CompoundTag();
            List<ItemStack> items = entry.getValue();
            for (int i = 0; i <= 4; i++) {
                Tag itemData;
                if (i >= items.size()) {
                    itemData = ServerUtil.saveItem(ItemStack.EMPTY, ops, prefix);
                } else {
                    itemData = ServerUtil.saveItem(items.get(i), ops, prefix);
                }
                bagData.put(String.valueOf(i), itemData);
            }
            playerData.put("bagItems", bagData);
            nbt.put(String.valueOf(entry.getKey()), playerData);
        }
        CompoundTag disabledPlayers = new CompoundTag();
        for (UUID disabled : this.disabledPlayers) {
            disabledPlayers.putString(disabled.toString(), "true");
        }
        nbt.put("disabledPlayers", disabledPlayers);
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
            version = nbtIn.getInt("version").get();
        }
        while (version < PLAYER_STORAGES_VERSION) {
            if (version == 1) {
                CompoundTag newNBT = new CompoundTag();
                Set<String> keys = nbtIn.keySet();
                for (String uuidStr : keys) {
                    CompoundTag oldPlayerData = nbtIn.getCompound(uuidStr).get();
                    CompoundTag bagItems = oldPlayerData.getCompound("storages").get().getCompound("0").get().getCompound("data").get();
                    bagItems.remove("identifier");
                    bagItems.remove("numOfItems");
                    for (int i = 0; i <= 4; i++) {
                        CompoundTag itemData = bagItems.getCompound("item" + i).get();
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
