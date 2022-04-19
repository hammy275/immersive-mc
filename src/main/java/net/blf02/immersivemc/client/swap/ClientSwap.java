package net.blf02.immersivemc.client.swap;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.common.network.Network;
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
        if (slot != 2) {
            // Put our true ItemStack there to be grabbed at anvil-ing time.
            ItemStack[] storage = isReallyAnvil ? ClientStorage.anvilStorage : ClientStorage.smithingStorage;
            storage[slot] = Minecraft.getInstance().player.getItemInHand(hand);
            storage[2] = ItemStack.EMPTY; // Clear output if we change something
            // Check that both item slots are occupied by something
            if (!storage[0].isEmpty() && !storage[1].isEmpty()) {
                Network.INSTANCE.sendToServer(new GetAnvilOutputPacket(storage[0], storage[1], isReallyAnvil));
            }
        }
    }

}
