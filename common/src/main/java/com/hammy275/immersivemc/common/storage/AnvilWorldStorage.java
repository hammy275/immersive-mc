package com.hammy275.immersivemc.common.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class AnvilWorldStorage extends ImmersiveStorage {

    public static final String TYPE = "anvil_store";

    public int xpLevels = 0;

    public AnvilWorldStorage(SavedData storage) {
        super(storage);
    }

    @Override
    public String getType() {
        return TYPE;
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
