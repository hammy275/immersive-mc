package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.SafeClientUtil;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InventorySwapPacket {

    public final int slot;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public InventorySwapPacket(int invSlotRaw) {
        this.slot = invSlotRaw;
    }

    public static void encode(InventorySwapPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.placementMode).writeInt(packet.slot);
    }

    public static InventorySwapPacket decode(FriendlyByteBuf buffer) {
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
                Swap.handleInventorySwap(player, message.slot, InteractionHand.MAIN_HAND);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
