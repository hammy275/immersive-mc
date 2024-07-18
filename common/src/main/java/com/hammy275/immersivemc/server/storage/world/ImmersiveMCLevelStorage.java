package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds ALL the save data for ImmersiveMC for a given world/dimension.
 */
public class ImmersiveMCLevelStorage extends SavedData {

    private static final int LEVEL_STORAGE_VERSION = 2;

    private static final String DATA_KEY = "immersivemc_data";
    protected Map<BlockPos, WorldStorage> storageMap = new HashMap<>();

    private static ImmersiveMCLevelStorage create() {
        return new ImmersiveMCLevelStorage();
    }

    public static ImmersiveMCLevelStorage getLevelStorage(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ImmersiveMCLevelStorage::load, ImmersiveMCLevelStorage::create, DATA_KEY);
    }

    @Nullable
    public WorldStorage remove(BlockPos pos) {
        return storageMap.remove(pos);
    }

    @Nullable
    public WorldStorage get(BlockPos pos, Level level) {
        WorldStorage storage = storageMap.get(pos);
        for (ImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
            if (handlerMaybeWS instanceof WorldStorageHandler<?> handler) {
                if (handler.getWorldStorageClass().isInstance(storage) && Util.isValidBlocks(handler, pos, level)) {
                    return storage;
                }
            }
        }
        return null;
    }

    @Nullable
    public WorldStorage getWithoutVerification(BlockPos pos, Level level) {
        return storageMap.get(pos);
    }

    @Nullable
    public WorldStorage getOrCreate(BlockPos pos, Level level) {
        WorldStorage storage = get(pos, level);
        if (storage != null) {
            return storage;
        }

        // At this point, we either didn't find a storage or the storage we found doesn't match with any handler.
        // Either way, attempt to make a new one.
        for (ImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
            if (handlerMaybeWS instanceof WorldStorageHandler<?> handler) {
                if (Util.isValidBlocks(handler, pos, level)) {
                    storage = handler.getEmptyWorldStorage();
                    storageMap.put(pos, storage);
                    return storage;
                }
            }
        }
        // At this point, we handle the potential circumstance that someone is upgrading from an old version of
        // ImmersiveMC from a pre-1.20 world, which leads to the extremely rare circumstance that we have an anvil
        // storage that needs to be converted into a smithing table one. This case is handled here, and is okay
        // to hardcode, since ImmersiveMC now effectively mandates each immersive has its own, unique ID, which
        // should prevent something like this from ever happening again.
        //
        // Note that we know this must be from a pre-1.20 world, as otherwise, the conversion would be detected
        // in ImmersiveMCLevelStorage#maybeUpgradeNBT
        storage = storageMap.get(pos);
        if (storage instanceof AnvilStorage as && ImmersiveHandlers.smithingTableHandler.isValidBlock(pos, level)) {
            SmithingTableStorage sts = new SmithingTableStorage();
            sts.copyFromOld(as);
            storageMap.put(pos, sts);
            this.setDirty();
            return sts;
        }

        // Storage wasn't in-memory, and we couldn't make a new one. Return null.
        return null;
    }

    public static void unmarkAllDirty(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            ImmersiveMCLevelStorage storage = level.getDataStorage().get(ImmersiveMCLevelStorage::load, DATA_KEY);
            if (storage != null) {
                storage.storageMap.forEach((pos, ws) -> {
                    if (ws instanceof ItemStorage is) {
                        is.setNoLongerDirtyForClientSync();
                    }
                });
            }
        }
    }

    public static ImmersiveMCLevelStorage load(CompoundTag nbt) {
        ImmersiveMCLevelStorage levelStorage = new ImmersiveMCLevelStorage();
        nbt = maybeUpgradeNBT(nbt);
        Map<BlockPos, WorldStorage> storageMap = levelStorage.storageMap;
        storageMap.clear();
        int numOfStorages = nbt.getInt("numOfStorages");

        CompoundTag storages = nbt.getCompound("storages");
        for (int i = 0; i < numOfStorages; i++) {
            CompoundTag storageInfo = storages.getCompound(String.valueOf(i));

            BlockPos pos = new BlockPos(storageInfo.getInt("posX"),
                    storageInfo.getInt("posY"),
                    storageInfo.getInt("posZ"));

            ResourceLocation id = Util.getResourceLocation(storageInfo, "id");
            WorldStorage storage = null;
            for (ImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.HANDLERS) {
                if (handlerMaybeWS.getID().equals(id) && handlerMaybeWS instanceof WorldStorageHandler<?> handler) {
                    storage = handler.getEmptyWorldStorage();
                    storage.load(storageInfo.getCompound("data"));
                    break;
                }
            }

            if (storage != null) {
                storageMap.put(pos, storage);
            }
        }
        return levelStorage;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("version", LEVEL_STORAGE_VERSION);
        nbt.putInt("numOfStorages", storageMap.size());
        CompoundTag storages = new CompoundTag();
        int i = 0;
        for (Map.Entry<BlockPos, WorldStorage> entry : this.storageMap.entrySet()) {
            CompoundTag storageInfo = new CompoundTag();

            storageInfo.putInt("posX", entry.getKey().getX());
            storageInfo.putInt("posY", entry.getKey().getY());
            storageInfo.putInt("posZ", entry.getKey().getZ());
            storageInfo.put("data", entry.getValue().save(new CompoundTag()));
            Util.putResourceLocation(storageInfo, "id", entry.getValue().getHandler().getID());

            storages.put(String.valueOf(i), storageInfo);
            i++;
        }

        nbt.put("storages", storages);
        return nbt;
    }

    /**
     * Upgrades NBT tag to something this version of ImmersiveMC can understand.
     * @param nbtIn NBT to upgrade. This may be modified in any way.
     * @return A converted NBT, that isn't necessarily the same object as the nbt going into this function.
     */
    private static CompoundTag maybeUpgradeNBT(CompoundTag nbtIn) {
        int version = 1;
        if (nbtIn.contains("version")) { // Version 1 didn't store a version int
            version = nbtIn.getInt("version");
        }
        while (version < LEVEL_STORAGE_VERSION) {
            if (version == 1) {
                int numOfStorages = nbtIn.getInt("numOfStorages");
                CompoundTag storages = nbtIn.getCompound("storages");
                for (int i = 0; i < numOfStorages; i++) {
                    CompoundTag storage = storages.getCompound(String.valueOf(i));
                    String oldDataType = storage.getString("dataType");
                    storage.remove("dataType");
                    CompoundTag itemsData = storage.getCompound("data");
                    String oldIdentifier = itemsData.getString("identifier");
                    itemsData.remove("identifier");
                    int numItems = itemsData.getInt("numOfItems");
                    ResourceLocation id;
                    if (numItems == 10) {
                        id = new ResourceLocation(ImmersiveMC.MOD_ID, "crafting_table");
                    } else if (numItems == 3 && oldDataType.equals("basic_item_store")) {
                        id = new ResourceLocation(ImmersiveMC.MOD_ID, "smithing_table");
                    } else if (numItems == 3) {
                        id = new ResourceLocation(ImmersiveMC.MOD_ID, "anvil");
                    } else if (numItems == 1) {
                        // Need to decode the item to figure out if this is an enchanting table or a beacon.
                        ItemStack item = ItemStack.of(itemsData.getCompound("item0"));
                        if (item.is(ItemTags.BEACON_PAYMENT_ITEMS)) {
                            id = new ResourceLocation(ImmersiveMC.MOD_ID, "beacon");
                        } else {
                            id = new ResourceLocation(ImmersiveMC.MOD_ID, "enchanting_table");
                        }
                    } else {
                        continue;
                    }
                    Util.putResourceLocation(storage, "id", id);
                }
            }
            version++;
        }
        return nbtIn;
    }
}
