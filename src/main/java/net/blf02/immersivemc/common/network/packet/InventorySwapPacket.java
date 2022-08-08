package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.SafeClientUtil;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class InventorySwapPacket {

    public final int slot;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public InventorySwapPacket(int invSlotRaw) {
        this.slot = invSlotRaw;
    }

    public static void encode(InventorySwapPacket packet, PacketBuffer buffer) {
        buffer.writeEnum(packet.placementMode).writeInt(packet.slot);
    }

    public static InventorySwapPacket decode(PacketBuffer buffer) {
        PlacementMode mode = buffer.readEnum(PlacementMode.class);
        InventorySwapPacket packet = new InventorySwapPacket(buffer.readInt());
        packet.placementMode = mode;
        return packet;
    }

    public static void handle(InventorySwapPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useBackpack) return;
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Swap.handleInventorySwap(player, message.slot, Hand.MAIN_HAND);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
