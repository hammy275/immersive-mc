package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.handler.WorldStorageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

public class SmithingTableStorage extends ItemStorage {
    public SmithingTableStorage() {
        super(4, 2);
    }

    @Override
    public WorldStorageHandler<SmithingTableStorage> getHandler() {
        return ImmersiveHandlers.smithingTableHandler;
    }

    @Override
    public void load(CompoundTag nbt, RegistryOps<Tag> ops, int lastVanillaDataVersion) {
        super.load(nbt, ops, lastVanillaDataVersion);
        if (nbt.getInt("numOfItems").get() == 3) { // Converting from 1.19 to 1.20
            convertFrom119();
        }
    }

    public void convertFrom119() {
        this.moveSlot(1, 2);
        this.moveSlot(0, 1);
        this.addSlotsToEnd(1);
    }
}
