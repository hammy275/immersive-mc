package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.mixin.SavedDataStorageAccessor;
import com.hammy275.immersivemc.server.ServerUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 * Holds ALL the save data for ImmersiveMC for a given world/dimension.
 */
public class ImmersiveMCLevelStorage extends SavedData {

    private static final int LEVEL_STORAGE_VERSION = 2;

    // These are special identifiers caught by SavedDataStorageMixin
    public static final Identifier V1_6_0_ALPHA3 = Util.mcId("immersivemc_data_v1_6_0_alpha3");
    public static final Identifier MC1_21_11_BELOW = Util.id("immersivemc_data_1_21_11_and_below");

    private static final Codec<ImmersiveMCLevelStorage> savedDataCodec = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ImmersiveMCLevelStorage, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(new Pair<>(ImmersiveMCLevelStorage.load(CompoundTag.CODEC.parse(ops, input).getOrThrow(), (RegistryOps<Tag>) ops), input));
        }

        @Override
        public <T> DataResult<T> encode(ImmersiveMCLevelStorage input, DynamicOps<T> ops, T prefix) {
            return CompoundTag.CODEC.encode(input.save(new CompoundTag(), (RegistryOps<Tag>) ops, (EndTag) prefix), ops, prefix);
        }
    };
    private static final SavedDataType<ImmersiveMCLevelStorage> savedDataType = new SavedDataType<>(
            Util.id("immersivemc_data"),
            ImmersiveMCLevelStorage::create,
            savedDataCodec,
            null
    );

    private static final SavedDataType<ImmersiveMCLevelStorage> savedDataType160Alpha3 = new SavedDataType<>(
            // Uses Minecraft namespace, since that's how world-upgrades should handle it.
            V1_6_0_ALPHA3,
            ImmersiveMCLevelStorage::create,
            savedDataCodec,
            null
    );
    private static final SavedDataType<ImmersiveMCLevelStorage> savedDataTypeMC12111AndBelow = new SavedDataType<>(
            MC1_21_11_BELOW,
            ImmersiveMCLevelStorage::create,
            savedDataCodec,
            null
    );


    protected Map<BlockPos, WorldStorage> storageMap = new HashMap<>();

    private static ImmersiveMCLevelStorage create() {
        return new ImmersiveMCLevelStorage();
    }

    public static ImmersiveMCLevelStorage getLevelStorage(ServerLevel level) {
        SavedDataStorage dataStorage = level.getDataStorage();
        ImmersiveMCLevelStorage storages = dataStorage.computeIfAbsent(savedDataType);
        // Upgrading from past versions in order of priority (data from MC 1.21.11 and below is preferred over
        // 1.6.0 Alpha 3, since the latter was not out for long).
        ImmersiveMCLevelStorage mc12111AndBelow = dataStorage.get(savedDataTypeMC12111AndBelow);
        ImmersiveMCLevelStorage v160Alpha3 = dataStorage.get(savedDataType160Alpha3);
        LinkedList<ImmersiveMCLevelStorage> oldStorages = new LinkedList<>();
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

    private static void mergeInOldStorages(ImmersiveMCLevelStorage storages, ImmersiveMCLevelStorage oldStorages) {
        // Doing a loop like this, since for ImmersiveMC 1.6.0 Alpha 3, this upgrade process did not occur.
        // As such, we don't want to remove data that may already be in the map.
        // During a regular upgrade, every entry will get added on the first merge, since what's being merged into
        // is in its default (empty) state.
        for (Map.Entry<BlockPos, WorldStorage> entry : oldStorages.storageMap.entrySet()) {
            if (!storages.storageMap.containsKey(entry.getKey())) {
                storages.storageMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Nullable
    public WorldStorage remove(BlockPos pos) {
        return storageMap.remove(pos);
    }

    @Nullable
    public WorldStorage get(BlockPos pos, Level level) {
        WorldStorage storage = storageMap.get(pos);
        for (BlockBasedImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.BLOCK_HANDLERS) {
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
        for (BlockBasedImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.BLOCK_HANDLERS) {
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
            sts.convertFrom119();
            storageMap.put(pos, sts);
            this.setDirty();
            return sts;
        }

        // Storage wasn't in-memory, and we couldn't make a new one. Return null.
        return null;
    }

    public static void unmarkAllItemStoragesDirty(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            ImmersiveMCLevelStorage storage = getLevelStorage(level);
            if (storage != null) {
                storage.storageMap.forEach((pos, ws) -> {
                    if (ws instanceof ItemStorage is) {
                        is.setNoLongerDirtyForClientSync();
                    }
                });
            }
        }
    }

    public static ImmersiveMCLevelStorage load(CompoundTag nbt, RegistryOps<Tag> ops) {
        ImmersiveMCLevelStorage levelStorage = new ImmersiveMCLevelStorage();
        // Use 3700 for 1.20.4 (most recent Minecraft version with ImmersiveMC before this was added) or the current Minecraft data version, whichever is lower.
        int lastVanillaDataVersion = nbt.contains("lastVanillaDataVersion") ? nbt.getInt("lastVanillaDataVersion").get() : Math.min(3700, SharedConstants.getCurrentVersion().dataVersion().version());
        nbt = maybeUpgradeNBT(nbt, ops, lastVanillaDataVersion);
        Map<BlockPos, WorldStorage> storageMap = levelStorage.storageMap;
        storageMap.clear();
        int numOfStorages = nbt.getInt("numOfStorages").get();

        CompoundTag storages = nbt.getCompound("storages").get();
        for (int i = 0; i < numOfStorages; i++) {
            CompoundTag storageInfo = storages.getCompound(String.valueOf(i)).get();

            BlockPos pos = new BlockPos(storageInfo.getInt("posX").get(),
                    storageInfo.getInt("posY").get(),
                    storageInfo.getInt("posZ").get());

            Identifier id = Util.getResourceLocation(storageInfo, "id");
            WorldStorage storage = null;
            for (BlockBasedImmersiveHandler<?> handlerMaybeWS : ImmersiveHandlers.BLOCK_HANDLERS) {
                if (handlerMaybeWS.getID().equals(id) && handlerMaybeWS instanceof WorldStorageHandler<?> handler) {
                    storage = handler.getEmptyWorldStorage();
                    storage.load(storageInfo.getCompound("data").get(), ops, lastVanillaDataVersion);
                    break;
                }
            }

            if (storage != null) {
                storageMap.put(pos, storage);
            }
        }
        return levelStorage;
    }

    public CompoundTag save(CompoundTag nbt, RegistryOps<Tag> ops, EndTag prefix) {
        nbt.putInt("lastVanillaDataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        nbt.putInt("version", LEVEL_STORAGE_VERSION);
        nbt.putInt("numOfStorages", storageMap.size());
        CompoundTag storages = new CompoundTag();
        int i = 0;
        for (Map.Entry<BlockPos, WorldStorage> entry : this.storageMap.entrySet()) {
            CompoundTag storageInfo = new CompoundTag();

            storageInfo.putInt("posX", entry.getKey().getX());
            storageInfo.putInt("posY", entry.getKey().getY());
            storageInfo.putInt("posZ", entry.getKey().getZ());
            storageInfo.put("data", entry.getValue().save(new CompoundTag(), ops, prefix));
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
     * @param ops Registry operations.
     * @param lastVanillaDataVersion The last vanilla data version this saved data was loaded in.
     * @return A converted NBT, that isn't necessarily the same object as the nbt going into this function.
     */
    private static CompoundTag maybeUpgradeNBT(CompoundTag nbtIn, RegistryOps<Tag> ops, int lastVanillaDataVersion) {
        int version = 1;
        if (nbtIn.contains("version")) { // Version 1 didn't store a version int
            version = nbtIn.getInt("version").get();
        }
        while (version < LEVEL_STORAGE_VERSION) {
            if (version == 1) {
                int numOfStorages = nbtIn.getInt("numOfStorages").get();
                CompoundTag storages = nbtIn.getCompound("storages").get();
                for (int i = 0; i < numOfStorages; i++) {
                    CompoundTag storage = storages.getCompound(String.valueOf(i)).get();
                    String oldDataType = storage.getString("dataType").get();
                    storage.remove("dataType");
                    CompoundTag itemsData = storage.getCompound("data").get();
                    String oldIdentifier = itemsData.getString("identifier").get();
                    itemsData.remove("identifier");
                    int numItems = itemsData.getInt("numOfItems").get();
                    Identifier id;
                    if (numItems == 10) {
                        id = Util.id("crafting_table");
                    } else if (numItems == 4 || (numItems == 3 && oldDataType.equals("basic_item_store"))) {
                        id = Util.id("smithing_table");
                    } else if (numItems == 3) {
                        id = Util.id("anvil");
                    } else if (numItems == 1) {
                        // Need to decode the item to figure out if this is an enchanting table or a beacon.
                        ItemStack item = ServerUtil.parseItem(ops, itemsData.getCompound("item0").get(), lastVanillaDataVersion);
                        if (item.is(ItemTags.BEACON_PAYMENT_ITEMS)) {
                            id = Util.id("beacon");
                        } else {
                            id = Util.id("enchanting_table");
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
