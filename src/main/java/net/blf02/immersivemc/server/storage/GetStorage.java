package net.blf02.immersivemc.server.storage;

import net.blf02.immersivemc.common.storage.AnvilStorage;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.SmithingTableBlock;
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

    public static int getLastInputIndex(BlockState state) {
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            return 8;
        } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
            return 1;
        } else if (state.getBlock() instanceof EnchantmentTableBlock) {
            return 0;
        }
        return -1;
    }

    public static ImmersiveStorage getPlayerStorage(Player player, String type) {
        List<ImmersiveStorage> storages = PlayerStorage.getStorages(player);
        for (ImmersiveStorage storage : storages) {
            if (storage.identifier.equals("backpack")) {
                return storage;
            }
        }
        if (type.equals("backpack")) {
            ImmersiveStorage storage = new ImmersiveStorage(PlayerStorage.getPlayerStorage(player)).initIfNotAlready(5);
            storage.identifier = "backpack";
            PlayerStorage.getStorages(player).add(storage);
            PlayerStorage.getPlayerStorage(player).setDirty();
            return storage;
        }
        throw new IllegalArgumentException("Invalid player storage type!");
    }

    public static ImmersiveStorage getStorage(Player player, BlockPos pos) {
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() == Blocks.CRAFTING_TABLE) {
            return getCraftingStorage(player, pos);
        } else if (state.getBlock() instanceof AnvilBlock || state.getBlock() instanceof SmithingTableBlock) {
            return getAnvilStorage(player, pos);
        } else if (state.getBlock() instanceof EnchantmentTableBlock) {
            return getEnchantingStorage(player, pos);
        }
        return null;
    }

    public static ImmersiveStorage getEnchantingStorage(Player player, BlockPos pos) {
        return LevelStorage.getStorage(player).getOrCreate(pos).initIfNotAlready(1);
    }

    public static AnvilStorage getAnvilStorage(Player player, BlockPos pos) {
        LevelStorage wStorage = LevelStorage.getStorage(player);
        ImmersiveStorage storageOld = wStorage.get(pos);
        AnvilStorage storage;
        if (!(storageOld instanceof AnvilStorage)) {
            storage = new AnvilStorage(wStorage);
            storage.initIfNotAlready(3);
            LevelStorage.getStorage(player).add(pos, storage);
        } else {
            storage = (AnvilStorage) storageOld;
        }
        return storage;
    }

    public static ImmersiveStorage getCraftingStorage(Player player, BlockPos pos) {
        return LevelStorage.getStorage(player).getOrCreate(pos).initIfNotAlready(10);
    }
}
