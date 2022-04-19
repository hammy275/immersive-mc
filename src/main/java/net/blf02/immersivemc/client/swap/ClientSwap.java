package net.blf02.immersivemc.client.swap;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.DoAnvilPacket;
import net.blf02.immersivemc.common.network.packet.GetAnvilOutputPacket;
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
        ItemStack[] storage = isReallyAnvil ? ClientStorage.anvilStorage : ClientStorage.smithingStorage;
        if (slot != 2) {
            // Put our true ItemStack there to be grabbed at anvil-ing time.
            storage[slot] = Minecraft.getInstance().player.getItemInHand(hand).copy();
            storage[2] = ItemStack.EMPTY; // Clear output if we change something
            // Check that both item slots are occupied by something
            if (!storage[0].isEmpty() && !storage[1].isEmpty()) {
                Network.INSTANCE.sendToServer(new GetAnvilOutputPacket(storage[0], storage[1], isReallyAnvil));
            }
        } else if (!storage[2].isEmpty()) { // Craft our result!
            int left = Minecraft.getInstance().player.inventory.findSlotMatchingItem(storage[0]);
            int mid = Minecraft.getInstance().player.inventory.findSlotMatchingItem(storage[1]);
            if (left != -1 && mid != -1) {
                Network.INSTANCE.sendToServer(new DoAnvilPacket(left, mid, pos));
                storage[0] = ItemStack.EMPTY;
                storage[1] = ItemStack.EMPTY;
                storage[2] = ItemStack.EMPTY;
            }
        }
    }

}
