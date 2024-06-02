package com.hammy275.immersivemc.server.storage.world;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import net.minecraft.nbt.CompoundTag;

public interface WorldStorage {

    /**
     * Load from the NBT tag into this object.
     * @param nbt NBT tag to load from.
     */
    public void load(CompoundTag nbt);

    /**
     * Save this object into the NBT tag.
     * @param nbt NBT tag to save to.
     * @return The same NBT tag as provided to this method.
     */
    public CompoundTag save(CompoundTag nbt);

    /**
     * @return Handler for this type of WorldStorage.
     */
    public ImmersiveHandler<? extends NetworkStorage> getHandler();

}
