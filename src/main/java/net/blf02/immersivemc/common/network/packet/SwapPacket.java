package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.SafeClientUtil;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.*;
import net.minecraftforge.network.NetworkEvent;

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

    public static void handle(final SwapPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && NetworkUtil.safeToRun(message.block, player)) {
                BlockEntity tileEnt = player.level.getBlockEntity(message.block);
                if (tileEnt instanceof AbstractFurnaceBlockEntity && ActiveConfig.useFurnaceImmersion) {
                    AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) tileEnt;
                    Swap.handleFurnaceSwap(furnace, player, message.hand, message.slot, message.placementMode);
                } else if (tileEnt instanceof BrewingStandBlockEntity && ActiveConfig.useBrewingImmersion) {
                    BrewingStandBlockEntity stand = (BrewingStandBlockEntity) tileEnt;
                    Swap.handleBrewingSwap(stand, player, message.hand, message.slot, message.placementMode);
                } else if (tileEnt instanceof JukeboxBlockEntity && ActiveConfig.useJukeboxImmersion) {
                    Swap.handleJukebox((JukeboxBlockEntity) tileEnt, player, message.hand);
                } else if (tileEnt instanceof ChestBlockEntity && ActiveConfig.useChestImmersion) {
                    Swap.handleChest((ChestBlockEntity) tileEnt, player, message.hand, message.slot);
                } else if (tileEnt instanceof EnderChestBlockEntity && ActiveConfig.useChestImmersion) {
                    Swap.handleEnderChest(player, message.hand, message.slot);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
