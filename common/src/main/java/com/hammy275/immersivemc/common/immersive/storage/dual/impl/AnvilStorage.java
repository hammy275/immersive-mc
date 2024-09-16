package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.api.common.immersive.WorldStorageHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class AnvilStorage extends ItemStorage {

    public int xpLevels = 0;

    public AnvilStorage() {
        super(3, 1);
    }

    @Override
    public WorldStorageHandler<AnvilStorage> getHandler() {
        return ImmersiveHandlers.anvilHandler;
    }

    @Override
    public void load(CompoundTag nbt, HolderLookup.Provider provider, int lastVanillaDataVersion) {
        super.load(nbt, provider, lastVanillaDataVersion);
        this.xpLevels = nbt.getInt("xpLevels");
    }

    @Override
    public CompoundTag save(CompoundTag nbtIn, HolderLookup.Provider provider) {
        CompoundTag nbt = super.save(nbtIn, provider);
        nbt.putInt("xpLevels", xpLevels);
        return nbt;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeInt(this.xpLevels);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        this.xpLevels = buffer.readInt();
    }
}
