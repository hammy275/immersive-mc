package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class InventorySwapPacket {

    public final int slot;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public InventorySwapPacket(int invSlotRaw) {
        this.slot = invSlotRaw;
    }

    public static void encode(InventorySwapPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(packet.placementMode).writeInt(packet.slot);
    }

    public static InventorySwapPacket decode(RegistryFriendlyByteBuf buffer) {
        PlacementMode mode = buffer.readEnum(PlacementMode.class);
        InventorySwapPacket packet = new InventorySwapPacket(buffer.readInt());
        packet.placementMode = mode;
        return packet;
    }

    public static void handle(InventorySwapPacket message, ServerPlayer player) {
        if (!ActiveConfig.FILE_SERVER.useBagImmersive) return;
        if (player != null) {
            Swap.handleInventorySwap(player, message.slot, InteractionHand.MAIN_HAND);
        }
    }
}
