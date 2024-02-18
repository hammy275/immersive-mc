package com.hammy275.immersivemc.server.storage;

import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.ImmersiveHandlers;
import com.hammy275.immersivemc.common.storage.AnvilWorldStorage;
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
        } else if (storageType.equals(AnvilWorldStorage.TYPE)) {
            storage = new AnvilWorldStorage(wStorage);
            storage.load(nbt);
        }

        if (storage == null) {
            throw new IllegalArgumentException("Storage type " + storageType + " does not exist!");
        }
        return storage;
    }

    // Used because we don't want to drop outputs on the ground, so we need the last input index
    public static int getLastInputIndex(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        if (ImmersiveHandlers.craftingHandler.isValidBlock(pos, state, tileEntity, level)) {
            return 8;
        } else if (ImmersiveHandlers.anvilHandler.isValidBlock(pos, state, tileEntity, level)) {
            return 1;
        } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEntity, level)) {
            return 0;
        } else if (ImmersiveHandlers.beaconHandler.isValidBlock(pos, state, tileEntity, level)) {
            return 0;
        } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEntity, level)) {
            return 2;
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
        BlockState state = player.level().getBlockState(pos);
        BlockEntity tileEnt = player.level().getBlockEntity(pos);
        if (ImmersiveHandlers.craftingHandler.isValidBlock(pos, state, tileEnt, player.level())) {
            return getCraftingStorage(player, pos);
        } else if (ImmersiveHandlers.anvilHandler.isValidBlock(pos, state, tileEnt, player.level())) {
            return getAnvilStorage(player, pos);
        } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEnt, player.level())) {
            return getEnchantingStorage(player, pos);
        } else if (ImmersiveHandlers.beaconHandler.isValidBlock(pos, state, tileEnt, player.level())) {
            return getBeaconStorage(player, pos);
        } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEnt, player.level())) {
            return getSmithingTableStorage(player, pos);
        }
        return null;
    }

    public static void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage storage = getStorage(player, pos);
        if (storage != null) {
            BlockState state = player.level().getBlockState(pos);
            BlockEntity tileEnt = player.level().getBlockEntity(pos);
            if (ImmersiveHandlers.craftingHandler.isValidBlock(pos, state, tileEnt, player.level())) {
                ItemStack out = Swap.getRecipeOutput(player, storage.getItemsRaw());
                storage.setItem(9, out);
            } else if (ImmersiveHandlers.anvilHandler.isValidBlock(pos, state, tileEnt, player.level()) &&
                storage instanceof AnvilWorldStorage aStorage) {
                Pair<ItemStack, Integer> out = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
                aStorage.xpLevels = out.getSecond();
                aStorage.setItem(2, out.getFirst());
            } else if (ImmersiveCheckers.isSmithingTable(pos, state, tileEnt, player.level())) {
                ItemStack out = Swap.getSmithingTableOutput(storage.getItem(0), storage.getItem(1),
                        storage.getItem(2), player);
                storage.setItem(3, out);
            } else if (ImmersiveCheckers.isEnchantingTable(pos, state, tileEnt, player.level())) {
                // No-op
            } else if (ImmersiveHandlers.beaconHandler.isValidBlock(pos, state, tileEnt, player.level())) {
                // No-op
            }
        }

    }

    public static ImmersiveStorage getEnchantingStorage(Player player, BlockPos pos) {
        return ImmersiveMCLevelStorage.getLevelStorage(player).getOrCreate(pos).initIfNotAlready(1);
    }

    public static AnvilWorldStorage getAnvilStorage(Player player, BlockPos pos) {
        ImmersiveMCLevelStorage wStorage = ImmersiveMCLevelStorage.getLevelStorage(player);
        ImmersiveStorage storageOld = wStorage.get(pos);
        AnvilWorldStorage storage;
        if (!(storageOld instanceof AnvilWorldStorage)) {
            storage = new AnvilWorldStorage(wStorage);
            storage.initIfNotAlready(3);
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, storage);
        } else {
            storage = (AnvilWorldStorage) storageOld;
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
        if (storageOld instanceof AnvilWorldStorage) {
            toRet = new ImmersiveStorage(wStorage);
            toRet.initIfNotAlready(4);
            for (int i = 0; i <= 2; i++) {
                // Offset index by 1 because of smithing table. No need to move item counts
                // as AnvilStorage was made into a different storage before item counts were added.
                toRet.getItemsRaw()[i  + 1] = storageOld.getItemsRaw()[i];
            }
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, toRet);
        } else if (storageOld == null) { // Create the storage normally if not found
            toRet = new ImmersiveStorage(wStorage);
            toRet.initIfNotAlready(4);
            ImmersiveMCLevelStorage.getLevelStorage(player).add(pos, toRet);
        } else { // Use the pre-existing storage
            toRet = storageOld;
        }
        if (toRet.getItemsRaw().length == 3) {
            // Convert up for 1.20's smithing templates. Don't move the output, since it might be a
            // 1.19 --> 1.20 world upgrade.
            toRet.moveSlot(1, 2);
            toRet.moveSlot(0, 1);
            toRet.addSlotsToEnd(1);
        }
        return toRet;
    }
}
