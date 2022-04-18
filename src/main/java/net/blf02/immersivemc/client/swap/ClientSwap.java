package net.blf02.immersivemc.client.swap;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.minecraft.block.AnvilBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class ClientSwap {

    public static void craftingSwap(int slot, Hand hand) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack playerItemCopy = Minecraft.getInstance().player.getItemInHand(hand).copy();
        playerItemCopy.setCount(1);
        ClientStorage.craftingStorage[slot] = playerItemCopy;

    }

    public static void anvilSwap(int slot, Hand hand, BlockPos pos) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        boolean isReallyAnvil = Minecraft.getInstance().level.getBlockState(pos).getBlock() instanceof AnvilBlock;
        if (slot != 2) {
            // Put our true ItemStack there to be grabbed at anvil-ing time.
            if (isReallyAnvil) {
                ClientStorage.anvilStorage[slot] = Minecraft.getInstance().player.getItemInHand(hand);
            } else {
                ClientStorage.smithingStorage[slot] = Minecraft.getInstance().player.getItemInHand(hand);
            }
        }
    }

}
