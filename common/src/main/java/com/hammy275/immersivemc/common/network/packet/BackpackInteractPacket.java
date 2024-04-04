package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.storage.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.function.Supplier;

public class BackpackInteractPacket {

    public final int slot;
    public final InteractionHand hand;
    public final PlacementMode placementMode;

    public BackpackInteractPacket(int slot, InteractionHand hand) {
        this(slot, hand, SafeClientUtil.getPlacementMode(true));
    }

    public BackpackInteractPacket(int slot, InteractionHand hand, PlacementMode placementMode) {
        this.slot = slot;
        this.hand = hand;
        this.placementMode = placementMode;
    }

    public static void encode(BackpackInteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
        buffer.writeEnum(packet.placementMode);
    }

    public static BackpackInteractPacket decode(FriendlyByteBuf buffer) {
        return new BackpackInteractPacket(buffer.readInt(),
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                buffer.readEnum(PlacementMode.class));
    }

    public static void handle(final BackpackInteractPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) {
                // -27 below since 0-26 are inventory slots
                Swap.handleBackpackCraftingSwap(message.slot - 27, message.hand,
                        ImmersiveMCPlayerStorages.getBackpackCraftingStorage(player), player, message.placementMode);
            }
        });
        
    }

}
