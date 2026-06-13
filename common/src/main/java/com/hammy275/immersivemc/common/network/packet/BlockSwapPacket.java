package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.api_impl.ItemSwapAmountImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public class BlockSwapPacket {

    public final BlockPos block;
    public final List<Integer> slots;
    public final InteractionHand hand;
    public final SwapMode mode;

    public BlockSwapPacket(BlockPos block, List<Integer> slots, InteractionHand hand, SwapMode mode) {
        this.block = block;
        this.slots = slots;
        this.hand = hand;
        this.mode = mode;
    }


    public static void encode(BlockSwapPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slots.size());
        for (Integer i : packet.slots) {
            buffer.writeInt(i);
        }
        buffer.writeBlockPos(packet.block);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
        buffer.writeEnum(packet.mode);
    }

    public static BlockSwapPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Integer> slots = new ArrayList<>(Math.min(size, 32));
        for (int i = 0; i < size; i++) {
            slots.add(buffer.readInt());
        }
        BlockSwapPacket ret = new BlockSwapPacket(buffer.readBlockPos(), slots,
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                buffer.readEnum(SwapMode.class));
        return ret;
    }

    public static void handle(final BlockSwapPacket message, ServerPlayer player) {
        int handStackSize = player.getItemInHand(message.hand).getCount();
        if (NetworkUtil.safeToRun(message.block, player)) {
            for (BlockBasedImmersiveHandler<?> handler : ImmersiveHandlers.BLOCK_HANDLERS) {
                if (handler.enabledInConfig(player) && Util.isValidBlocks(handler, message.block, player.level)) {
                    for (int i = 0; i < message.slots.size(); i++) {
                        ItemSwapAmount swapAmount = new ItemSwapAmountImpl(message.mode, message.slots.size(), handStackSize, i);
                        handler.swap(message.slots.get(i), message.hand, message.block, player, swapAmount);
                    }
                    break;
                }
            }
        }

    }
}
