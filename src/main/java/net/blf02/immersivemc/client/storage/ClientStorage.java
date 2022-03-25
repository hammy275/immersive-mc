package net.blf02.immersivemc.client.storage;

import net.minecraft.inventory.CraftingInventory;

public class ClientStorage {

    // Is only used for handling recipes, so setting this to null is fine
    public static CraftingInventory craftingStorage = new CraftingInventory(new NullContainer(),
            3, 3);

}
