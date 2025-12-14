package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;

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
    public void load(CompoundTag nbt, RegistryOps<CompoundTag> ops, int lastVanillaDataVersion) {
        super.load(nbt, ops, lastVanillaDataVersion);
        this.xpLevels = nbt.getInt("xpLevels").get();
    }

    @Override
    public CompoundTag save(CompoundTag nbtIn, RegistryOps<CompoundTag> ops, CompoundTag prefix) {
        CompoundTag nbt = super.save(nbtIn, ops, prefix);
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
