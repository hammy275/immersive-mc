package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.api_impl.ItemSwapAmountImpl;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public class SwapPacket {

    public final BlockPos block;
    public final List<Integer> slots;
    public final InteractionHand hand;
    public final SwapMode mode;
    public final SwapDestination destination;

    public SwapPacket(BlockPos block, List<Integer> slots, InteractionHand hand, SwapMode mode) {
        this(block, slots, hand, mode, SwapDestination.POS);
    }

    public SwapPacket(BlockPos block, List<Integer> slots, InteractionHand hand, SwapMode mode, SwapDestination destination) {
        this.block = block;
        this.slots = slots;
        this.hand = hand;
        this.mode = mode;
        this.destination = destination;
    }


    public static void encode(SwapPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.slots.size());
        for (Integer i : packet.slots) {
            buffer.writeInt(i);
        }
        buffer.writeBlockPos(packet.block);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
        buffer.writeEnum(packet.mode);
        buffer.writeEnum(packet.destination);
    }

    public static SwapPacket decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Integer> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            slots.add(buffer.readInt());
        }
        SwapPacket ret = new SwapPacket(buffer.readBlockPos(), slots,
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                buffer.readEnum(SwapMode.class),
                buffer.readEnum(SwapDestination.class));
        return ret;
    }

    public static void handle(final SwapPacket message, ServerPlayer player) {
        int handStackSize = player.getItemInHand(message.hand).getCount();
        switch (message.destination) {
            case POS -> {
                if (NetworkUtil.safeToRun(message.block, player)) {
                    for (ImmersiveHandler<?> handler : ImmersiveHandlers.HANDLERS) {
                        if (handler.enabledInConfig(player) && Util.isValidBlocks(handler, message.block, player.level())) {
                            for (int i = 0; i < message.slots.size(); i++) {
                                ItemSwapAmount swapAmount = new ItemSwapAmountImpl(message.mode, message.slots.size(), handStackSize, i);
                                handler.swap(message.slots.get(i), message.hand, message.block, player, swapAmount);
                            }
                            break;
                        }
                    }
                }
            }
            case INVENTORY -> {
                if (!ActiveConfig.FILE_SERVER.useBagImmersive) return;
                if (player != null) {
                    for (int slot : message.slots) {
                        Swap.handleInventorySwap(player, slot, InteractionHand.MAIN_HAND);
                    }
                }
            }
            case BAG_CRAFTING -> {
                if (player != null) {
                    // -27 below since 0-26 are inventory slots
                    for (int i = 0; i < message.slots.size(); i++) {
                        ItemSwapAmount swapAmount = new ItemSwapAmountImpl(message.mode, message.slots.size(), handStackSize, i);
                        Swap.handleBackpackCraftingSwap(message.slots.get(i) - 27, message.hand,
                                ImmersiveMCPlayerStorages.getBackpackCraftingStorage(player), player, swapAmount);
                    }
                }
            }
        }

    }

    public enum SwapDestination {
        POS, INVENTORY, BAG_CRAFTING
    }
}
