package net.blf02.immersivemc.common.storage;

import net.blf02.immersivemc.server.storage.WorldStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;

/**
 * Holds all info for a given block's storage.
 */
public class ImmersiveStorage {

    /**
     * Instance of WorldStorage. Will always exist server-side and NEVER exist client-side.
     * Do not use this in a constructor! Clients also use the constructor!
     */
    public final WorldStorage wStorage;

    /**
     * A unique String representing the type of storage this is. Used in WorldStorage when saving/loading NBT.
     */
    public static final String TYPE = "basic_item_store";

    /**
     * A list of items. Usually contains inputs and outputs.
     */
    public ItemStack[] items;

    public ImmersiveStorage(WorldStorage storage) {
        this.wStorage = storage;
    }

    /**
     * Initializes this storage ONLY IF IT ISN'T ALREADY!!!
     * @param numOfItems Number of items to store
     * @return This object.
     */
    public ImmersiveStorage initIfNotAlready(int numOfItems) {
        if (items == null) {
            items = new ItemStack[numOfItems];
            Arrays.fill(items, ItemStack.EMPTY);
            this.wStorage.setDirty();
        }
        return this;
    }

    /**
     * Used to determine which storage type is being loaded from disk. MUST BE CHANGED FOR ANYTHING THAT
     * EXTENDS THIS CLASS, AND IT MUST BE UNIQUE!!!
     * @return A String ID of what type of storage instance this is
     */
    public String getType() {
        return TYPE;
    }


    public void load(CompoundNBT nbt) {
        int length = nbt.getInt("numOfItems");
        this.items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            this.items[i] = ItemStack.of(nbt.getCompound("item" + i));
        }
    }

    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putInt("numOfItems", items.length);
        for (int i = 0; i < items.length; i++) {
            nbt.put("item" + i, items[i].save(new CompoundNBT()));
        }
        return nbt;
    }
}
