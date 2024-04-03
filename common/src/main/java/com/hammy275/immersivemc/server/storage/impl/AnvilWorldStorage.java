package com.hammy275.immersivemc.server.storage.impl;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import net.minecraft.nbt.CompoundTag;

public class AnvilWorldStorage extends ItemWorldStorage {

    public int xpLevels = 0;

    public AnvilWorldStorage() {
        super(3, 1);
    }

    @Override
    public ImmersiveHandler getHandler() {
        return ImmersiveHandlers.anvilHandler;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.xpLevels = nbt.getInt("xpLevels");
    }

    @Override
    public CompoundTag save(CompoundTag nbtIn) {
        CompoundTag nbt = super.save(nbtIn);
        nbt.putInt("xpLevels", xpLevels);
        return nbt;
    }
}
