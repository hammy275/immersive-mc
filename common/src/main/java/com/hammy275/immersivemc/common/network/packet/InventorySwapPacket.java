package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import dev.architectury.networking.NetworkManager;

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

    public static void handle(InventorySwapPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            if (!ActiveConfig.FILE_SERVER.useBagImmersive) return;
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) {
                Swap.handleInventorySwap(player, message.slot, InteractionHand.MAIN_HAND);
            }
        });
        
    }
}
