package com.hammy275.immersivemc.server.storage.world;

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
    private static Factory<ImmersiveMCPlayerStorages> factory = new Factory<>(
            ImmersiveMCPlayerStorages::create,
            ImmersiveMCPlayerStorages::load,
            null
    );

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
        if (!player.level().isClientSide) {
            ServerPlayer sPlayer = (ServerPlayer) player;
            return sPlayer.getServer().overworld().getDataStorage()
                    .computeIfAbsent(factory, "immersivemc_player_data");
        }
        throw new IllegalArgumentException("Can only access storage on server-side!");
    }

    public static ImmersiveMCPlayerStorages load(CompoundTag nbt) {
        ImmersiveMCPlayerStorages playerStorage = new ImmersiveMCPlayerStorages();
        Set<String> keys = nbt.getAllKeys();
        for (String uuidStr : keys) {
            UUID uuid = UUID.fromString(uuidStr);
            CompoundTag bagItems = nbt.getCompound(uuidStr).getCompound("bagItems");
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i <= 4; i++) {
                items.add(ItemStack.of(bagItems.getCompound(String.valueOf(i))));
            }
            playerStorage.backpackCraftingItemsMap.put(uuid, items);
        }
        return playerStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
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
}
