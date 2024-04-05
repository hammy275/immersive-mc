package com.hammy275.immersivemc.common.immersive.storage.dual.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import net.minecraft.nbt.CompoundTag;

public class SmithingTableStorage extends ItemStorage {
    public SmithingTableStorage() {
        super(4, 2);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.smithingTableHandler;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.getInt("numOfItems") == 3) { // Converting from 1.19 to 1.20
            convertFrom119();
        }
    }

    public void convertFrom119() {
        this.moveSlot(1, 2);
        this.moveSlot(0, 1);
        this.addSlotsToEnd(1);
    }
}
