package net.blf02.immersivemc.client.swap;

import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.DoETablePacket;
import net.blf02.immersivemc.common.network.packet.GetEnchantmentsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ClientSwap {

    public static void eTableSwap(int immersiveSlot, Hand hand, BlockPos pos) {
        if (Minecraft.getInstance().player == null) return;
        if (immersiveSlot == 0) {
            ItemStack item = Minecraft.getInstance().player.getItemInHand(hand).copy();
            if (!item.isEmpty() && !item.isEnchantable()) return;
            ClientStorage.resetEnchs();
            ClientStorage.eTableItem = item;
            if (!item.isEmpty()) {
                Map<Enchantment, Integer> fakeEnchs = new HashMap<>();
                fakeEnchs.put(Enchantments.MENDING, 1);
                ClientStorage.eTableEnchCopy = ClientStorage.eTableItem.copy();
                EnchantmentHelper.setEnchantments(fakeEnchs, ClientStorage.eTableEnchCopy);
            }
            Network.INSTANCE.sendToServer(new GetEnchantmentsPacket(item, pos));
        } else {
            int itemSlot = Minecraft.getInstance().player.inventory.findSlotMatchingItem(ClientStorage.eTableItem);
            if (itemSlot > -1) {
                Network.INSTANCE.sendToServer(new DoETablePacket(itemSlot, hand, pos, immersiveSlot));
            }
            ClientStorage.resetEnchs();
        }
    }

}
