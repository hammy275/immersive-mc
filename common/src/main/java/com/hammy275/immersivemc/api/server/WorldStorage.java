package com.hammy275.immersivemc.api.server;

import com.google.common.annotations.Beta;
import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.nbt.CompoundTag;

/**
 * An object that can be written to and from NBT used with a {@link WorldStorageHandler}. Allows Immersives to hold
 * items and persist them between world shutdowns if the block doesn't already handle holding items. For example,
 * ImmersiveMC uses this for vanilla crafting tables, since crafting tables can't hold items on their own.
 */
@Beta
public interface WorldStorage {

    /**
     * Load from the NBT tag into this object.
     *
     * @param nbt NBT tag to load from.
     * @param lastVanillaDataVersion The last vanilla data version this storage was loaded in.
     */
    public void load(CompoundTag nbt, int lastVanillaDataVersion);

    /**
     * Save this object into the NBT tag.
     * @param nbt NBT tag to save to.
     * @return The same NBT tag as provided to this method.
     */
    public CompoundTag save(CompoundTag nbt);

    /**
     * @return Handler for this type of WorldStorage.
     */
    public WorldStorageHandler<? extends NetworkStorage> getHandler();

}
