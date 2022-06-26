package net.blf02.immersivemc.common.storage;

import net.blf02.immersivemc.server.storage.WorldStorage;
import net.minecraft.nbt.CompoundNBT;

public class AnvilStorage extends ImmersiveStorage {

    public static final String TYPE = "anvil_store";

    public int xpLevels = 0;

    public AnvilStorage(WorldStorage storage) {
        super(storage);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void load(CompoundNBT nbt) {
        super.load(nbt);
        this.xpLevels = nbt.getInt("xpLevels");
    }

    @Override
    public CompoundNBT save(CompoundNBT nbtIn) {
        CompoundNBT nbt = super.save(nbtIn);
        nbt.putInt("xpLevels", xpLevels);
        return nbt;
    }
}
