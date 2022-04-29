package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class InventorySwapPacket {

    public final int slot;

    public InventorySwapPacket(int invSlotRaw) {
        this.slot = invSlotRaw;
    }

    public static void encode(InventorySwapPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.slot);
    }

    public static InventorySwapPacket decode(PacketBuffer buffer) {
        return new InventorySwapPacket(buffer.readInt());
    }

    public static void handle(InventorySwapPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useBackpack) return;
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                Swap.handleInventorySwap(player, message.slot, Hand.MAIN_HAND);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
