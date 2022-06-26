package net.blf02.immersivemc.server.storage.info;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ImmersiveStorage {

    public static final String TYPE = "basic_item_store";

    public ItemStack[] items;

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
