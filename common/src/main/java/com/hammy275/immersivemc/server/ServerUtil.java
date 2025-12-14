package com.hammy275.immersivemc.server;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

public class ServerUtil {

    private static final int CURRENT_VANILLA_DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();
    private static final CompoundTag EMPTY_ITEM = new CompoundTag();

    /**
     * Saves an item to NBT, including allowing saving of empty ItemStack's.
     * @param stack ItemStack to save.
     * @param ops Registry operations.
     * @param prefix Prefix
     * @return Item saved to an NBT tag.
     */
    public static Tag saveItem(ItemStack stack, RegistryOps<Tag> ops, EndTag prefix) {
        if (stack.isEmpty()) {
            return EMPTY_ITEM.copy();
        } else {
            return ItemStack.CODEC.encode(stack, ops, prefix).resultOrPartial().orElse(EMPTY_ITEM.copy());
        }
    }

    /**
     * Loads an item from NBT, upgrading it between Minecraft versions as needed.
     * @param ops Registry operations.
     * @param nbt The NBT being loaded.
     * @param lastVanillaDataVersion The last vanilla data version used to save the item.
     * @return The loaded ItemStack.
     */
    public static ItemStack parseItem(RegistryOps<Tag> ops, CompoundTag nbt, int lastVanillaDataVersion) {
        if (nbt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (CURRENT_VANILLA_DATA_VERSION > lastVanillaDataVersion) {
            nbt = (CompoundTag) DataFixers.getDataFixer().update(References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE, nbt), lastVanillaDataVersion, CURRENT_VANILLA_DATA_VERSION).getValue();
        }
        return ItemStack.CODEC.parse(ops, nbt).resultOrPartial().orElse(ItemStack.EMPTY);
    }
}
