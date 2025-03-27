package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.server.ServerUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Uses SavedData to hold player storage
 */
public class ImmersiveMCPlayerStorages extends SavedData {

    private static final int PLAYER_STORAGES_VERSION = 2;


    private static final SavedDataType<ImmersiveMCPlayerStorages> savedDataType = new SavedDataType<>(
            "immersivemc_player_data",
            ImmersiveMCPlayerStorages::create,
            ctx -> new Codec<>() {
                @Override
                public <T> DataResult<Pair<ImmersiveMCPlayerStorages, T>> decode(DynamicOps<T> ops, T input) {
                    return DataResult.success(new Pair<>(ImmersiveMCPlayerStorages.load(CompoundTag.CODEC.parse(ops, input).getOrThrow(), ctx.levelOrThrow().registryAccess()), input));
                }

                @Override
                public <T> DataResult<T> encode(ImmersiveMCPlayerStorages input, DynamicOps<T> ops, T prefix) {
                    return CompoundTag.CODEC.encode(input.save(new CompoundTag(), ctx.levelOrThrow().registryAccess()), ops, prefix);
                }
            },
            null
    );

    protected Map<UUID, List<ItemStack>> backpackCraftingItemsMap = new HashMap<>();
    protected Set<UUID> disabledPlayers = new HashSet<>();

    private static ImmersiveMCPlayerStorages create(Context context) {
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
        if (!player.level().isClientSide) {
            ServerPlayer sPlayer = (ServerPlayer) player;
            return sPlayer.getServer().overworld().getDataStorage()
                    .computeIfAbsent(savedDataType);
        }
        throw new IllegalArgumentException("Can only access storage on server-side!");
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

    public static ImmersiveMCPlayerStorages load(CompoundTag nbt, HolderLookup.Provider provider) {
        ImmersiveMCPlayerStorages playerStorage = new ImmersiveMCPlayerStorages();
        // Use 3700 for 1.20.4 (most recent Minecraft version with ImmersiveMC before this was added) or the current Minecraft data version, whichever is lower.
        int lastVanillaDataVersion = nbt.contains("lastVanillaDataVersion") ? nbt.getInt("lastVanillaDataVersion").get() : Math.min(3700, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        nbt = maybeUpgradeNBT(nbt, lastVanillaDataVersion);
        Set<String> keys = nbt.keySet();
        for (String uuidStr : keys) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag bagItems = nbt.getCompound(uuidStr).get().getCompound("bagItems").get();
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i <= 4; i++) {
                    items.add(ServerUtil.parseItem(provider, bagItems.getCompound(String.valueOf(i)).get(), lastVanillaDataVersion));
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

    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.putInt("lastVanillaDataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        nbt.putInt("version", PLAYER_STORAGES_VERSION);
        for (Map.Entry<UUID, List<ItemStack>> entry : backpackCraftingItemsMap.entrySet()) {
            CompoundTag playerData = new CompoundTag();
            CompoundTag bagData = new CompoundTag();
            List<ItemStack> items = entry.getValue();
            for (int i = 0; i <= 4; i++) {
                Tag itemData;
                if (i >= items.size()) {
                    itemData = ServerUtil.saveItem(ItemStack.EMPTY, provider);
                } else {
                    itemData = ServerUtil.saveItem(items.get(i), provider);
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
