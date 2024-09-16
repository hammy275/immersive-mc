package com.hammy275.immersivemc.server;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

public class ServerUtil {

    private static final int CURRENT_VANILLA_DATA_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

    /**
     * Loads an item from NBT, upgrading it between Minecraft versions as needed.
     * @param nbt The NBT being loaded.
     * @param lastVanillaDataVersion The last vanilla data version used to save the item.
     * @return The loaded ItemStack.
     */
    public static ItemStack parseItem(CompoundTag nbt, int lastVanillaDataVersion) {
        if (CURRENT_VANILLA_DATA_VERSION > lastVanillaDataVersion) {
            nbt = (CompoundTag) DataFixers.getDataFixer().update(References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, nbt), lastVanillaDataVersion, CURRENT_VANILLA_DATA_VERSION).getValue();
        }
        return ItemStack.of(nbt);
    }
}
