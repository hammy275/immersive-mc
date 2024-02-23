package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.server.swap.Swap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class SwapPacket {

    public final BlockPos block;
    public final int slot;
    public final InteractionHand hand;
    public PlacementMode placementMode = SafeClientUtil.getPlacementMode();

    public SwapPacket(BlockPos block, int slot, InteractionHand hand) {
        this.block = block;
        this.slot = slot;
        this.hand = hand;
    }

    public static void encode(SwapPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.placementMode);
        buffer.writeBlockPos(packet.block);
        buffer.writeInt(packet.slot);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
    }

    public static SwapPacket decode(FriendlyByteBuf buffer) {
        PlacementMode mode = buffer.readEnum(PlacementMode.class);
        SwapPacket packet = new SwapPacket(buffer.readBlockPos(), buffer.readInt(),
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        packet.placementMode = mode;
        return packet;
    }

    public static void handle(final SwapPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(message.block, player)) {
                BlockEntity tileEnt = player.level().getBlockEntity(message.block);
                BlockState state = player.level().getBlockState(message.block);
                for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
                    if (handler.enabledInServerConfig() && handler.isValidBlock(message.block, state, tileEnt, player.level())) {
                        handler.swap(message.slot, message.hand, message.block, player, message.placementMode);
                    }
                }
                if (ImmersiveCheckers.isChest(message.block, state, tileEnt, player.level())
                        && ActiveConfig.FILE.useChestImmersion) {
                    if (tileEnt instanceof ChestBlockEntity cbe) {
                        Swap.handleChest(cbe, player, message.hand, message.slot);
                    } else if (tileEnt instanceof EnderChestBlockEntity) {
                        Swap.handleEnderChest(player, message.hand, message.slot);
                    }
                } else if (ImmersiveCheckers.isIronFurnacesFurnace(message.block, state, tileEnt, player.level())
                        && ActiveConfig.FILE.useIronFurnacesFurnaceImmersion) {
                    ImmersiveHandlers.furnaceHandler.swap(message.slot, message.hand, message.block, player, message.placementMode);
                }
            }
        });
        
    }


}
