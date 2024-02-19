package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.function.Supplier;

/**
 * Used when interacting with something that uses WorldStorage
 */
public class InteractPacket {

    public final BlockPos pos;
    public final String storageKey;
    public final int slot;
    public final InteractionHand hand;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public InteractPacket(BlockPos pos, int slot, InteractionHand hand) {
        this.pos = pos;
        this.slot = slot;
        this.hand = hand;

        this.storageKey = null;
    }

    public InteractPacket(String storageKey, int slot, InteractionHand hand) {
        this.storageKey = storageKey;
        this.slot = slot;
        this.hand = hand;

        this.pos = null;

        if (storageKey.equals("backpack")) {
            placementMode = SafeClientUtil.getPlacementMode(true);
        }
    }

    protected InteractPacket setPlacementMode(PlacementMode mode) {
        this.placementMode = mode;
        return this;
    }

    public boolean isPlayerStorageInteract() {
        return this.storageKey != null;
    }

    public static void encode(InteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.placementMode);
        buffer.writeBoolean(packet.isPlayerStorageInteract());
        if (packet.isPlayerStorageInteract()) {
            buffer.writeUtf(packet.storageKey);
        } else {
            buffer.writeBlockPos(packet.pos);
        }
        buffer.writeInt(packet.slot).writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
    }

    public static InteractPacket decode(FriendlyByteBuf buffer) {
        PlacementMode mode = buffer.readEnum(PlacementMode.class);
        if (buffer.readBoolean()) {
            return new InteractPacket(buffer.readUtf(), buffer.readInt(),
                    buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).setPlacementMode(mode);
        } else {
            return new InteractPacket(buffer.readBlockPos(), buffer.readInt(),
                    buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).setPlacementMode(mode);
        }
    }

    public static void handle(final InteractPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (message.isPlayerStorageInteract()) {
                if (message.storageKey.equals("backpack")) {
                    ImmersiveStorage storage = GetStorage.getPlayerStorage(player, "backpack");
                    // -27 below since 0-26 are inventory slots
                    Swap.handleBackpackCraftingSwap(message.slot - 27, message.hand, storage, player, message.placementMode);
                }
            }
        });
        
    }

}
