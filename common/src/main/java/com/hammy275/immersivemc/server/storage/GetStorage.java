package com.hammy275.immersivemc.server.storage;

import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.storage.AnvilStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;

public class GetStorage {

    public static ImmersiveStorage assembleStorage(CompoundTag nbt, String storageType, SavedData wStorage) {
        // Check storage type, and load storage accordingly
        ImmersiveStorage storage = null;
        if (storageType.equals(ImmersiveStorage.TYPE)) {
            storage = new ImmersiveStorage(wStorage);
            storage.load(nbt);
        } else if (storageType.equals(AnvilStorage.TYPE)) {
            storage = new AnvilStorage(wStorage);
            storage.load(nbt);
        }

        if (storage == null) {
            throw new IllegalArgumentException("Storage type " + storageType + " does not exist!");
        }
        return storage;
    }

    // Used because we don't want to drop outputs on the ground, so we need the last input index
    public static int getLastInputIndex(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        if (ImmersiveCheckers.isCraftingTable(pos, state, tileEntity, level)) {
            return 8;
        } else if (ImmersiveCheckers.isAnvil(pos, state, tileEntity, level)) {
            return 1;
        } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEntity, level)) {
            return 0;
        } else if (ImmersiveCheckers.isBeacon(pos, state, tileEntity, level)) {
            return 0;
        } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEntity, level)) {
            return 1;
        }
        throw new RuntimeException("Last input index not defined for the block that was just broken!");
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

    public static ImmersiveStorage getStorage(Player player, BlockPos pos) {
        BlockState state = player.level.getBlockState(pos);
        BlockEntity tileEnt = player.level.getBlockEntity(pos);
        if (ImmersiveCheckers.isCraftingTable(pos, state, tileEnt, player.level)) {
            return getCraftingStorage(player, pos);
        } else if (ImmersiveCheckers.isAnvil(pos, state, tileEnt, player.level)) {
            return getAnvilStorage(player, pos);
        } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEnt, player.level)) {
            return getEnchantingStorage(player, pos);
        } else if (ImmersiveCheckers.isBeacon(pos, state, tileEnt, player.level)) {
            return getBeaconStorage(player, pos);
        } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEnt, player.level)) {
            return getSmithingTableStorage(player, pos);
        }
        return null;
    }

    public static void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage storage = getStorage(player, pos);
        if (storage != null) {
            BlockState state = player.level.getBlockState(pos);
            BlockEntity tileEnt = player.level.getBlockEntity(pos);
            if (ImmersiveCheckers.isCraftingTable(pos, state, tileEnt, player.level)) {
                ItemStack out = Swap.getRecipeOutput(player, storage.getItemsRaw());
                storage.setItem(9, out);
            } else if (ImmersiveCheckers.isAnvil(pos, state, tileEnt, player.level) &&
                storage instanceof AnvilStorage aStorage) {
                Pair<ItemStack, Integer> out = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
                aStorage.xpLevels = out.getSecond();
                aStorage.setItem(2, out.getFirst());
            } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEnt, player.level)) {
                ItemStack out = Swap.getSmithingTableOutput(storage.getItem(0), storage.getItem(1), player);
                storage.setItem(2, out);
            } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEnt, player.level)) {
                // No-op
            } else if (ImmersiveCheckers.isBeacon(pos, state, tileEnt, player.level)) {
                // No-op
            }
        }

    }

    public static ImmersiveStorage getEnchantingStorage(Player player, BlockPos pos) {
        return ImmersiveMCLevelStorage.getLevelStorage(player).getOrCreate(pos).initIfNotAlready(1);
    }

    public static AnvilStorage getAnvilStorage(Player player, BlockPos pos) {
        ImmersiveMCLevelStorage wStorage = ImmersiveMCLevelStorage.getLevelStorage(player);
        ImmersiveStorage storageOld = wStorage.get(pos);
        AnvilStorage storage;
        if (!(storageOld instanceof AnvilStorage)) {
            storage = new AnvilStorage(wStorage);
            storage.initIfNotAlready(3);
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, storage);
        } else {
            storage = (AnvilStorage) storageOld;
        }
        return storage;
    }

    public static ImmersiveStorage getCraftingStorage(Player player, BlockPos pos) {
        return ImmersiveMCLevelStorage.getLevelStorage(player).getOrCreate(pos).initIfNotAlready(10);
    }

    public static ImmersiveStorage getBeaconStorage(Player player, BlockPos pos) {
        return ImmersiveMCLevelStorage.getLevelStorage(player).getOrCreate(pos).initIfNotAlready(1);
    }

    public static ImmersiveStorage getSmithingTableStorage(Player player, BlockPos pos) {
        ImmersiveMCLevelStorage wStorage = ImmersiveMCLevelStorage.getLevelStorage(player);
        ImmersiveStorage storageOld = wStorage.get(pos);
        ImmersiveStorage toRet;
        // May be AnvilStorage from before the anvil/smithing table split (SAVE_DATA_VERSION 1 -> 2)
        if (storageOld instanceof AnvilStorage) {
            toRet = new ImmersiveStorage(wStorage);
            toRet.initIfNotAlready(3);
            for (int i = 0; i <= 2; i++) {
                toRet.getItemsRaw()[i] = storageOld.getItemsRaw()[i];
            }
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, toRet);
        } else if (storageOld == null) {
            toRet = new ImmersiveStorage(wStorage);
            toRet.initIfNotAlready(3);
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, toRet);
        } else {
            toRet = storageOld;
        }
        return toRet;
    }
}
