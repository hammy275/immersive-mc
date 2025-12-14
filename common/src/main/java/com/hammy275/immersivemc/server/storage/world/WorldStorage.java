package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

/**
 * An object that can be written to and from NBT used with a {@link WorldStorageHandler}. Allows Immersives to hold
 * items and persist them between world shutdowns if the block doesn't already handle holding items. For example,
 * ImmersiveMC uses this for vanilla crafting tables, since crafting tables can't hold items on their own.
 */
public interface WorldStorage {

    /**
     * Load from the NBT tag into this object.
     *
     * @param nbt NBT tag to load from.
     * @param ops Registry operations.
     * @param lastVanillaDataVersion The last vanilla data version this storage was loaded in.
     */
    public void load(CompoundTag nbt, RegistryOps<Tag> ops, int lastVanillaDataVersion);

    /**
     * Save this object into the NBT tag.
     *
     * @param nbt NBT tag to save to.
     * @param ops Registry operations.
     * @param prefix Prefix.
     * @return The same NBT tag as provided to this method.
     */
    public CompoundTag save(CompoundTag nbt, RegistryOps<Tag> ops, EndTag prefix);

    /**
     * @return Handler for this type of WorldStorage.
     */
    public WorldStorageHandler<? extends NetworkStorage> getHandler();

}
