package net.blf02.immersivemc.client.swap;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ClientSwap {

    public static void craftingSwap(int slot, Hand hand) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack playerItemCopy = Minecraft.getInstance().player.getItemInHand(hand).copy();
        playerItemCopy.setCount(1);
        ClientStorage.craftingStorage.setItem(slot, playerItemCopy);

    }

}
